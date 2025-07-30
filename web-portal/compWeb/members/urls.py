from django.conf import settings
from django.urls import path
from . import views
from django.contrib.auth import views as auth_views
from django.conf.urls.static import static

urlpatterns = [
    path('', views.members, name='members'),
    path('index/', views.index, name='index'),
    path('contact_form/', views.contactForm, name='contact_form'),
    path('newsletter_signup/', views.index, name='newsletter_signup'),
    path('adminLogin/', views.admin_login, name='adminLogin'),
    path('adminDashboard/', views.dashboard, name='adminDashboard'),
    path('billing/', views.billing, name='billing'),
    path('tables/', views.tables, name='tables'),
    path('update_status/<int:id>/', views.update_status, name='update_status'),
    path('notifications/', views.notification, name='notification'),
    path('logout/', auth_views.LogoutView.as_view(next_page='login'), name='logout'),
    path('login/', views.admin_login, name='login'),
    path('hiredEmp/', views.hiredEmp, name='hiredEmp'),
    path('offer-letter/', views.generate_offer_letter, name='offer_letter'),
    path('register/', views.register_applicant, name='register_applicant'),
    path('success/<int:applicant_id>/', views.registration_success, name='application_success'),
    path('chatbot/', views.chatbot, name='chatbot'),
]
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)