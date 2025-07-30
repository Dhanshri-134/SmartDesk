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
                ('HR', 'HR')
            ]),
            'resume': forms.ClearableFileInput(attrs={'class': 'form-control'}),
            'cover_letter': forms.Textarea(attrs={'class': 'form-control', 'rows': 4, 'placeholder': 'Write a brief cover letter...'}),
        }

    def clean_contact(self):
        contact = self.cleaned_data.get('contact')
        if not re.match(r'^\d{10}$', contact):
            raise forms.ValidationError("Phone number must be exactly 10 digits.")
        return contact
