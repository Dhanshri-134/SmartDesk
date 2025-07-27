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