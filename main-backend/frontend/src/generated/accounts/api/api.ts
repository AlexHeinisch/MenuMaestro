export * from './accounts.service';
import { AccountsApiService } from './accounts.service';
export * from './accounts.serviceInterface';
export * from './auth.service';
import { AuthApiService } from './auth.service';
export * from './auth.serviceInterface';
export const APIS = [AccountsApiService, AuthApiService];
