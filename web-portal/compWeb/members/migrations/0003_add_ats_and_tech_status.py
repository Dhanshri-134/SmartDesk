from django.db import migrations, models

class Migration(migrations.Migration):

    dependencies = [
        ('members', '0002_add_status_field'),
    ]

    operations = [
        migrations.AddField(
            model_name='applicant',
            name='ats_score',
            field=models.IntegerField(default=0),
        ),
        migrations.AddField(
            model_name='applicant',
            name='tech_round_status',
            field=models.CharField(
                max_length=10,
                choices=[('Pass', 'Pass'), ('Fail', 'Fail')],
                default='Fail'
            ),
        ),
    ]
