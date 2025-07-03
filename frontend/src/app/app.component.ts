import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FooterComponent } from './components/Layout/Footer';
import { HeaderComponent } from './components/Layout/Header';
import { TokenService } from './security/token.service';

@Component({
    selector: 'app-root',
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, HeaderComponent, FooterComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'MenuMaestro';

  constructor(private tokenService: TokenService) {}

  ngOnInit(): void {
    this.tokenService.tryRefreshRoles();
  }
}
