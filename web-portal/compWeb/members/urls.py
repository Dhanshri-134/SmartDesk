from django.urls import path
from . import views

urlpatterns = [
    path('members/', views.members, name='members'),
    path('index/', views.index, name='index'),
    path('blog/', views.blog, name='blog'),
    path('contact_form/', views.contactForm, name='contact_form'),
    path('newsletter_signup/', views.index, name='newsletter_signup'),
    path('home/', views.index, name='home'),
    path('offer-letter/', views.generate_offer_letter, name='offer_letter'),

]