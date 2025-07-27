from django.conf import settings
from django.urls import path
from . import views

from django.conf.urls.static import static

urlpatterns = [
    path('', views.members, name='members'),
    path('index/', views.index, name='index'),
    path('contact_form/', views.contactForm, name='contact_form'),
    path('newsletter_signup/', views.index, name='newsletter_signup'),
    path('adminLogin/', views.admin_login, name='admin_login'),
    path('adminDashboard/', views.dashboard, name='adminLogin'),
    path('hiredEmp/', views.hiredEmp, name='hiredEmp'),
    path('offer-letter/', views.generate_offer_letter, name='offer_letter'),

]
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)