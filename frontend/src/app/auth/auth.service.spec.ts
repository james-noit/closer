import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login should call correct URL and store tokens', () => {
    const dummyResp = { token: 'T', refreshToken: 'R', tokenType: 'Bearer' };
    service.login('u', 'p').subscribe(resp => {
      expect(resp).toEqual(dummyResp);
      expect(localStorage.getItem('closer_token')).toBe('T');
      expect(localStorage.getItem('closer_refresh')).toBe('R');
    });

    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResp);
  });

  it('logout should post to logout endpoint when refresh token present', () => {
    localStorage.setItem('closer_refresh', 'R');
    service.logout().subscribe(() => {});
    const req = http.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ refreshToken: 'R' });
    req.flush(null);
  });

  it('refresh should post to refresh endpoint and update tokens', () => {
    localStorage.setItem('closer_refresh', 'R');
    const dummyResp = { token: 'T2', refreshToken: 'R2', tokenType: 'Bearer' };
    service.refresh().subscribe(resp => {
      expect(resp).toEqual(dummyResp);
      expect(localStorage.getItem('closer_token')).toBe('T2');
      expect(localStorage.getItem('closer_refresh')).toBe('R2');
    });
    const req = http.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(req.request.method).toBe('POST');
    req.flush(dummyResp);
  });
});
