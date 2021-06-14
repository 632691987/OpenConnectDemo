package org.openconnect.oauth2.server.demo.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

@Controller
@SessionAttributes("authorizationRequest")
public class AuthorizationController {

  @RequestMapping("/oauth/confirm_access")
  public String getAccessConfirmation(Model model, HttpServletRequest request) throws Exception {
    final AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.getAttribute("authorizationRequest");
    String thirdPartyName = HtmlUtils.htmlEscape(authorizationRequest.getClientId());
    model.addAttribute("thirdPartyName", thirdPartyName);

    final CsrfToken csrfToken = (CsrfToken) (model.containsAttribute("_csrf") ? model.getAttribute("_csrf") : request.getAttribute("_csrf"));
    if (csrfToken != null) {
      String strCSRFToken = HtmlUtils.htmlEscape(csrfToken.getToken());
      model.addAttribute("csrfToken", strCSRFToken);
    }

    final Map<String, String> scopes = (Map<String, String>) (model.containsAttribute("scopes") ? model.getAttribute("scopes") : request.getAttribute("scopes"));
    model.addAttribute("scopes", scopes.keySet());

    return "authorization";
  }

}
