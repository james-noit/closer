export interface AuthenticationResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}
