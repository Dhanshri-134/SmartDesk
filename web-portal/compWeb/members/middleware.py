
from .models import Visitor
from django.utils.timezone import now

class VisitorLoggingMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        ip = request.META.get('REMOTE_ADDR')
        user_agent = request.META.get('HTTP_USER_AGENT', '')

        # Save to DB
        Visitor.objects.create(
            ip_address=ip,
            user_agent=user_agent,
            timestamp=now()
        )

        response = self.get_response(request)
        return response
