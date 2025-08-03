import os
import datetime
from datetime import timedelta
from django.conf import settings
from django.template.loader import get_template
from xhtml2pdf import pisa
from django.core.mail import EmailMessage


def link_callback(uri, rel):
    """
    Convert HTML URIs to absolute system paths so xhtml2pdf can access static and media files.
    """
    if uri.startswith(settings.MEDIA_URL):
        path = os.path.join(settings.MEDIA_ROOT, uri.replace(settings.MEDIA_URL, ""))
    elif uri.startswith(settings.STATIC_URL):
        path = os.path.join(settings.STATICFILES_DIRS[0], uri.replace(settings.STATIC_URL, ""))
    else:
        path = uri

    path = path.replace("\\", "/")
    if not os.path.isfile(path):
        raise Exception(f"Missing file: {path}")
    return path

def generate_joining_letter_pdf(applicant):
    try:
        # Create output directory
        output_dir = os.path.join(settings.MEDIA_ROOT, "joining_letters")
        os.makedirs(output_dir, exist_ok=True)

        pdf_filename = f"Joining_Letter_{applicant.name.replace(' ', '_')}.pdf"
        pdf_path = os.path.join(output_dir, pdf_filename)

        # Joining date (example: 2 days from now)
        joining_date = (datetime.date.today() + timedelta(days=2)).strftime("%d %B %Y")

        # Logo path (your favicon.png)
        logo_path = os.path.join(settings.STATICFILES_DIRS[0], "assets/img/favicon.png")
        if not os.path.isfile(logo_path):
            raise Exception(f"Logo file not found at {logo_path}")

        # Render template
        template = get_template("joining_letter.html")
        context = {
            "company_name": "SmartDesk Pvt Ltd",
            "today_date": datetime.date.today().strftime("%d %B %Y"),
            "employee_name": applicant.name,
            "job_role": applicant.role,
            "joining_date": joining_date,
            "username": applicant.email.split("@")[0].lower(),
            "password": "$1rEKEfjO4",
            "hr_name": "HR Team",
            "company_logo": logo_path.replace("\\", "/"), 
            "location": "Hinjawadi Phase-I, Pune",
            "manager_name": "Mr. David Farnendis",         
            "compensation": "₹6,00,000 per annum",      
            "hr_name": "HR Team"
        }

        html_content = template.render(context)

        # Generate PDF
        with open(pdf_path, "wb") as pdf_file:
            pisa_status = pisa.CreatePDF(html_content, dest=pdf_file, link_callback=link_callback)

        if pisa_status.err:
            print("[ERROR] PDF generation failed")
            return None

        return pdf_path

    except Exception as e:
        print(f"[ERROR] Generating Joining Letter PDF: {e}")
        return None




def send_joining_letter_email(applicant):
    try:
        pdf_path = generate_joining_letter_pdf(applicant)
        if not pdf_path:
            return False

        joining_date = (datetime.date.today() + timedelta(days=2)).strftime("%B %d, %Y")
        username = applicant.name.lower().replace(' ', '.')
        temp_password = "$1rEKEfjO4"

        subject = f"Joining Letter - {applicant.name}"
        body = f"""
Dear {applicant.name},

We are delighted to welcome you to SmartDesk Pvt Ltd.

🗓 Joining Date: {joining_date}  
👤 Username: {username}  
🔑 Temporary Password: {temp_password}  

Please find your official joining letter attached to this email.

Welcome to the team! 🚀

Best Regards,  
HR Team  
SmartDesk Pvt Ltd
"""

        email = EmailMessage(
            subject,
            body,
            settings.DEFAULT_FROM_EMAIL,
            [applicant.email],
        )
        email.attach_file(pdf_path)
        email.send()

        print(f"[INFO] Joining letter sent to {applicant.email}")
        return True

    except Exception as e:
        print(f"[ERROR] Sending Joining Letter Email: {e}")
        return False
