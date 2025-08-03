from django import forms
from .models import Applicant
import re

class ApplicantForm(forms.ModelForm):
    COUNTRY_CHOICES = [
        ('+91', '+91 (India)'),
        ('+1', '+1 (USA)'),
        ('+44', '+44 (UK)'),
       
    ]

    country_code = forms.ChoiceField(choices=COUNTRY_CHOICES)
    class Meta:
        model = Applicant
        fields = ['name', 'email', 'address', 'country_code', 'contact', 'role', 'resume', 'cover_letter']
        widgets = {
            'name': forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Enter full name'}),
            'email': forms.EmailInput(attrs={'class': 'form-control', 'placeholder': 'Enter your email'}),
            'address': forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Enter your address'}),
            'country_code': forms.Select(attrs={'class': 'form-select'}),
            'contact': forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Enter 10-digit phone'}),
            'role': forms.Select(attrs={'class': 'form-select'}, choices=[
                ('Software Developer', 'Software Developer'),
                ('QA Engineer', 'QA Engineer'),
                ('UI/UX Designer', 'UI/UX Designer'),
                ('Tech Lead', 'Tech Lead'),
                ('HR', 'HR'),
                ('Software Engineer','Software Engineer'),
                ('Software Tester','Software Tester'),
                ('Team Lead','Team Lead'),
                ('Project Manager','Project Manager'),
                ('Flutter Developer','Flutter Developer'),
                ('Java Developer','Java Developer'),
                ('Python Developer','Python Developer'),
                ('Java FullStack Developer','Java FullStack Developer'),
                ('Data Analyst','Data Analyst'),
                ('Data Scientist','Data Scientist'),
                ('Business Analyst','Business Analyst'),
                ('Web Developer', 'Web Developer'),
                ('Frontend Developer', 'Frontend Developer'),
                ('Backend Developer', 'Backend Developer'),
                ('Machine Learning Engineer', 'Machine Learning Engineer'),
                ('AI Engineer', 'AI Engineer'),
                ('DevOps Engineer', 'DevOps Engineer'),
                ('Database Administrator', 'Database Administrator'),
                ('System Analyst', 'System Analyst'),
                ('Cloud Engineer', 'Cloud Engineer'),
                ('Cybersecurity Analyst', 'Cybersecurity Analyst'),
            ]),
            'resume': forms.ClearableFileInput(attrs={'class': 'form-control'}),
            'cover_letter': forms.Textarea(attrs={'class': 'form-control', 'rows': 4, 'placeholder': 'Write a brief cover letter...'}),
        }

    def clean_contact(self):
        contact = self.cleaned_data.get('contact')
        if not re.match(r'^\d{10}$', contact):
            raise forms.ValidationError("Phone number must be exactly 10 digits.")
        return contact
