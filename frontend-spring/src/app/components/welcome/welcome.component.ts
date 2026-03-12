import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss'
})
export class WelcomeComponent {
  features = [
    { icon: '🕐', title: 'Gain de temps', description: 'Trouvez rapidement la garderie idéale pour votre enfant grâce à notre moteur de recherche avancé.' },
    { icon: '🛡️', title: 'Confiance et sécurité', description: 'Consultez les avis des autres parents et les informations vérifiées sur chaque garderie.' },
    { icon: '❤️', title: 'Suivi quotidien', description: 'Restez informé du programme quotidien et communiquez directement avec la garderie.' }
  ];
}
