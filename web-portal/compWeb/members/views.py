import datetime
import os
from django.conf import settings
from django.http import HttpResponse, JsonResponse
from django.shortcuts import get_object_or_404, render
from django.template import loader
from django.template.loader import render_to_string,get_template
from xhtml2pdf import pisa
from django.templatetags.static import static
import io
from django.contrib.auth import authenticate, login
from django.shortcuts import render, redirect
from django.contrib import messages
from members.models import Applicant
import re
from django.http import HttpResponse
from .models import Applicant
import os
import docx2txt
import PyPDF2
from .models import Applicant,Notification
from django.contrib.auth.decorators import login_required, user_passes_test
from .models import Visitor




def extract_text_from_resume(resume_file):
    ext = os.path.splitext(resume_file.name)[1].lower()
    if ext == '.pdf':
        pdf_reader = PyPDF2.PdfReader(resume_file)
        text = ''
        for page in pdf_reader.pages:
            text += page.extract_text() or ''
        return text
    elif ext == '.docx':
        return docx2txt.process(resume_file)
    else:
        return ''

def load_keywords():
    role_keywords = {}
    current_role = None
    keyword_file_path = os.path.join(settings.BASE_DIR, 'members', 'ats', 'keywords.txt')


    with open(keyword_file_path, 'r') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            if line.startswith('[') and line.endswith(']'):
                current_role = line[1:-1].strip()
                role_keywords[current_role] = []
            elif current_role:
                role_keywords[current_role].append(line.lower())
    
    return role_keywords

ROLE_KEYWORDS = load_keywords()


def calculate_ats_score(resume_text, role_keywords):
    resume_text = resume_text.lower()
    matched_keywords = [keyword for keyword in role_keywords if re.search(r'\b' + re.escape(keyword) + r'\b', resume_text)]
    print(f"Matched Keywords: {matched_keywords}")  # DEBUG

    max_possible_keywords = len(set(role_keywords))  # avoid duplicate keywords
    if max_possible_keywords == 0:
        return 0
    score = (len(matched_keywords) / max_possible_keywords) * 100
    return round(score, 2)


def register_applicant(request):
    if request.method == 'POST':
        form = ApplicantForm(request.POST, request.FILES)
        if form.is_valid():
            applicant = form.save(commit=False)
            applicant.country_code = form.cleaned_data['country_code']
            applicant.contact = form.cleaned_data['contact']

            resume_file = request.FILES['resume']
            resume_text = extract_text_from_resume(resume_file)
            print(f"Resume Text Extracted:\n{resume_text[:1000]}")  # DEBUG: print first 1000 chars

            role = form.cleaned_data['role']
            keywords = ROLE_KEYWORDS.get(role, []) + ROLE_KEYWORDS.get("Common", [])

            print(f"Loaded {len(keywords)} keywords for role: {role}")  # DEBUG

            ats_score = calculate_ats_score(resume_text, keywords)

            applicant.ats_score = ats_score
            applicant.status = 'Pending'
            applicant.save()

            messages.success(request, f'Application submitted! ATS Score: {ats_score}')
            return redirect('application_success', applicant_id=applicant.id)
    else:
        form = ApplicantForm()
    return render(request, 'registration_form.html', {'form': form})




def registration_success(request, applicant_id):
    applicant = get_object_or_404(Applicant, id=applicant_id)
    return render(request, 'success.html', {'applicant': applicant})



def extract_text_from_resume(resume_file):
    ext = os.path.splitext(resume_file.name)[1].lower()
    if ext == '.pdf':
        pdf_reader = PyPDF2.PdfReader(resume_file)
        text = ''
        for page in pdf_reader.pages:
            text += page.extract_text() or ''
        return text
    elif ext == '.docx':
        return docx2txt.process(resume_file)
    else:
        return ''

def load_keywords():
    role_keywords = {}
    current_role = None
    keyword_file_path = os.path.join(settings.BASE_DIR, 'members', 'ats', 'keywords.txt')


    with open(keyword_file_path, 'r') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            if line.startswith('[') and line.endswith(']'):
                current_role = line[1:-1].strip()
                role_keywords[current_role] = []
            elif current_role:
                role_keywords[current_role].append(line.lower())
    
    return role_keywords

ROLE_KEYWORDS = load_keywords()



def calculate_ats_score(resume_text, role_keywords):
    resume_text = resume_text.lower()
    matched_keywords = [keyword for keyword in role_keywords if re.search(r'\b' + re.escape(keyword) + r'\b', resume_text)]
    print(f"Matched Keywords: {matched_keywords}")  # DEBUG

    max_possible_keywords = len(set(role_keywords))  # avoid duplicate keywords
    if max_possible_keywords == 0:
        return 0
    score = (len(matched_keywords) / max_possible_keywords) * 100
    return round(score, 2)



def register_applicant(request):
    if request.method == 'POST':
        form = ApplicantForm(request.POST, request.FILES)
        if form.is_valid():
            applicant = form.save(commit=False)
            applicant.country_code = form.cleaned_data['country_code']
            applicant.contact = form.cleaned_data['contact']

            resume_file = request.FILES['resume']
            resume_text = extract_text_from_resume(resume_file)
            print(f"Resume Text Extracted:\n{resume_text[:1000]}")  # DEBUG: print first 1000 chars

            role = form.cleaned_data['role']
            keywords = ROLE_KEYWORDS.get(role, []) + ROLE_KEYWORDS.get("Common", [])

            print(f"Loaded {len(keywords)} keywords for role: {role}")  # DEBUG

            ats_score = calculate_ats_score(resume_text, keywords)

            applicant.ats_score = ats_score
            applicant.status = 'Pending'
            applicant.save()
            Notification.objects.create(
            title="New Applicant Received",
            message=f"{applicant.name} has applied for the {applicant.role} role."
)
            messages.success(request, f'Application submitted! ATS Score: {ats_score}')
            return redirect('application_success', applicant_id=applicant.id)
    else:
        form = ApplicantForm()
    return render(request, 'registration_form.html', {'form': form})



def tables(request):
    applicants = Applicant.objects.all()
    hired_applicants = Applicant.objects.filter(status='Hired')
    return render(request, 'adminDashboard/pages/tables.html', {
        'applicants': applicants,
        'hired_applicants': hired_applicants
    })

def registration_success(request, applicant_id):
    applicant = get_object_or_404(Applicant, id=applicant_id)
    return render(request, 'success.html', {'applicant': applicant})

def notification(request):
    applicants = Applicant.objects.all()
    return render(request, 'adminDashboard/pages/notifications.html', {'applicants': applicants})

def members(request):
  template = loader.get_template('index.html')
  return HttpResponse(template.render())


def index(request):
  template = loader.get_template('index.html')
  return HttpResponse(template.render())

 
def blog(request):
  template = loader.get_template('index.html')
  return HttpResponse(template.render())

def serviceDetails(request):
  template = loader.get_template('index.html')
  return HttpResponse(template.render())

def hiredEmp(request):
    applicants = Applicant.objects.all()
    return render(request, 'hiredEmp.html', {'applicants': applicants})

def contactForm(request):
  template = loader.get_template('index.html')
  return HttpResponse(template.render())

def dashboard(request):
    applicants = Applicant.objects.all()
    return render(request, 'adminDashboard.html', {'applicants': applicants})

def update_status(request, id):
    if request.method == "POST":
        applicant = get_object_or_404(Applicant, id=id)
        new_status = request.POST.get('status')
        applicant.status = new_status
        applicant.save()
        return JsonResponse({'success': True})


def generate_offer_letter(request):
    logo_path = r"D:\My Projects\Final Project\SmartDesk\web-portal\compWeb\members\static\assets\img\favicon.png"
    signature_path =r"D:\My Projects\Final Project\SmartDesk\web-portal\compWeb\members\static\assets\img\logo.webp"
    context = {
    'company_logo': logo_path,
    'signature_img': f'{signature_path.replace("\\", "/")}',
    'today_date': datetime.date.today().strftime("%B %d, %Y"),
    'employee_name': 'John Doe',
    'employee_address': '123 Main Street, Mumbai',
    'company_name': 'SmartDesk Pvt Ltd',
    'job_role': 'Software Engineer',
    'joining_date': 'August 1, 2025',
    'username': 'john.doe',
    'password': 'secure@123',
    'hr_name': 'Ms. Priya Sharma',
}

    template = get_template("offer_letter.html")
    html = template.render(context)
    
    result = io.BytesIO()
    pdf = pisa.pisaDocument(io.BytesIO(html.encode("UTF-8")), result)

    if not pdf.err:
        return HttpResponse(result.getvalue(), content_type='application/pdf')

    return HttpResponse("Error while generating PDF", status=500)

def billing(request):
    applicants = Applicant.objects.all()
    return render(request, 'adminDashboard/pages/billing.html', {'applicants': applicants})


def admin_login(request):
    if request.method == 'POST':
        email = request.POST.get('email')
        password = request.POST.get('password')
        user = authenticate(request, username=email, password=password)
        if user is not None and user.is_staff: 
            login(request, user)
            return redirect('adminDashboard')  #
        else:
            messages.error(request, 'Invalid credentials or not an admin.')

    return render(request, 'admin_login.html')

@login_required
@user_passes_test(lambda u: u.is_staff)
def admin_dashboard(request):
    return render(request, 'adminDashboard.html')  


def create_applicant(request):
    if request.method == 'POST':
        # your applicant creation logic here
        applicant =0 # NewApplicant.objects.create(
             # Add other fields as necessary
        # )

        # Create a notification
        Notification.objects.create(
            title="New Applicant Received",
            message=f"{applicant.name} has applied for a {applicant.role}.",
        )

        #redirect to the TECH but temperorily redirect to Admin dashboard
        return redirect('success-page')
    

import re

def chatbot(request):
    message = request.GET.get("message", "").strip().lower()

    # 1. Application Status Intent
    match = re.search(r'(?P<name>[a-z ]+)\s+(?P<id>[a-z0-9]+)', message)
    if 'status' in message or match:
        if match:
            name = match.group("name").title()
            app_id = match.group("id").upper()

            try:
                app = Applicant.objects.get(applicant_id=app_id, name__iexact=name)
                return JsonResponse({"reply": f"Status for {name} (ID: {app_id}) is: {app.status}"})
            except Applicant.DoesNotExist:
                return JsonResponse({"reply": "No application found with that name and ID."})
        return JsonResponse({"reply": "Please enter your full name followed by application ID, e.g. 'Riya A123'."})

    # 2. Small Talk
    if any(word in message for word in ["hi", "hello", "hey"]):
        return JsonResponse({"reply": "Hello! 👋 I'm SmartDesk Bot. Ask me about your application, job roles, or interview."})
    if "thank" in message:
        return JsonResponse({"reply": "You're welcome! 😊 Let me know if you need anything else."})
    if "who are you" in message:
        return JsonResponse({"reply": "I'm SmartDesk Assistant, here to help you with job application tracking and support."})

    # 3. Show Job Openings
    if "job" in message or "openings" in message:
        # In real case, fetch from DB
        jobs = ["Java Developer", "Frontend Intern", "ML Engineer", "HR Assistant"]
        job_list = "\n• " + "\n• ".join(jobs)
        return JsonResponse({"reply": f"Current job openings:\n{job_list}"})

    # 4. Interview Date (mocked example)
    if "interview" in message:
        return JsonResponse({"reply": "If shortlisted, your interview date will be sent via email. You can also check your application status here."})

    # 5. Contact HR
    if "contact" in message or "hr" in message:
        return JsonResponse({"reply": "You can reach our HR team at hr@smartdesk.com or call +91-9876543210."})

    # 6. Resume Upload
    if "upload" in message or "resume" in message:
        return JsonResponse({"reply": "You can upload your resume at: https://smartdesk.com/upload-resume"})

    # 7. Help Guide
    if "help" in message or "how to apply" in message:
        return JsonResponse({"reply": "To apply, go to the Careers page, choose a role, and submit your resume. You’ll receive a confirmation email."})

    # 8. Fallback
    return JsonResponse({"reply": "I'm not sure how to help with that yet. Try asking about job status, openings, interview, or HR contact."})

