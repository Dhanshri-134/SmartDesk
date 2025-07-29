from django.utils import timezone
from django.db import models



class Applicant(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()
    role = models.CharField(max_length=100)
    ats_score = models.IntegerField()
    tech_round_status = models.CharField(max_length=10, choices=[('Pass', 'Pass'), ('Fail', 'Fail')])
    status = models.CharField(max_length=20, default="Pending")  # Hired, Rejected, Pending
    resume = models.FileField(upload_to='resumes/')
    cover_letter = models.TextField(blank=True)

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