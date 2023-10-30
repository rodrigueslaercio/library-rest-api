package project.library.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import project.library.repositories.UserRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class Authorization extends OncePerRequestFilter{

    @Autowired
    UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var path = request.getServletPath();
        var method = request.getMethod();

        if((path.startsWith("/users")  && !method.equals("POST")) || (path.startsWith("/books") && !method.equals("GET"))) {

            var authorization  = request.getHeader("Authorization");
            var authEncoded = authorization.substring("Basic".length()).trim();
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecode);
            String[] credentials = authString.split(":");
            String email = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByEmail(email);
            if(user == null) {
                response.sendError(401);
            } else {
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passwordVerify.verified) {
                    request.setAttribute("idUser", user.getIdUser());
                    filterChain.doFilter(request, response);
                }else {
                    response.sendError(401);
                }
            }

        } else {
            filterChain.doFilter(request, response);
        }

    }
}
