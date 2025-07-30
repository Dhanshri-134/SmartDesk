from django.utils import timezone
from django.db import models

COUNTRY_CODES = [
    ('+91', '+91 India'),
    ('+1', '+1 USA'),
    ('+44', '+44 UK'),
    ('+61', '+61 Australia'),
    ('+81', '+81 Japan'),
]

STATUS_CHOICES = [
    ('Pending', 'Pending'),
    ('Accepted', 'Accepted'),
    ('Rejected', 'Rejected'),
]

class Applicant(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()
    country_code = models.CharField(max_length=5, choices=COUNTRY_CODES, default='+91')
    contact = models.CharField(max_length=10)
    address = models.TextField()  
    role = models.CharField(max_length=100)
    resume = models.FileField(upload_to='resumes/')

    cover_letter = models.TextField(blank=True)
    contact = models.CharField(max_length=20)
    address = models.TextField(blank=True)

    def __str__(self):
        return self.name
    
class Visitor(models.Model):
    ip_address = models.GenericIPAddressField()
    user_agent = models.TextField()
    timestamp = models.DateTimeField(default=timezone.now)

    def __str__(self):
        return f"{self.ip_address} - {self.timestamp}"   
    

class Notification(models.Model):
    title = models.CharField(max_length=255)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    is_read = models.BooleanField(default=False)

    def __str__(self):
        return self.title

    cover_letter = models.TextField(blank=True, null=True)
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default='Pending')  #reject or hire
    ats_score = models.FloatField(default=0.0)
    
    def __str__(self):
        return self.name

