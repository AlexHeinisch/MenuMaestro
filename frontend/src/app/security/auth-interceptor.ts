import { inject } from "@angular/core";
import { HttpInterceptorFn } from "@angular/common/http";
import { TokenService } from "./token.service";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);

  // Do not intercept authentication requests
  if (!tokenService.isAuthenticated()) {
    return next(req);
  }

  const authReq = req.clone({
    headers: req.headers.set(
      "Authorization",
      "Bearer " + tokenService.getToken(),
    ),
  });

  return next(authReq);
};
