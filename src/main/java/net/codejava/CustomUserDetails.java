// para adaptar la entidad user de la bd a ser utilizada en nuestro proyecto de spring.
package net.codejava;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // consumimos de este framework para manejar 
                                                                // los detalles de un usuario en la base de datos
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private User user; // declaracion

    public CustomUserDetails(User user) { // constructor
        this.user = user; 
    }

    @Override // obtener el rol del usuario
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("User role: " + user.getRole()); // Depuración
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override // para obtener password de usuario
    public String getPassword() {
        return user.getPassword();
    }

    @Override // obtener su username
    public String getUsername() {
        return user.getEmail();
    }

    @Override // propiedades de la cuenta
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override // propiedad enabled en la base de datos
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public String getFullName() { // concatenamos el nombre y apellidos para obtener el nombre del usuario
        return user.getFirstName() + " " + user.getLastName();
    }
}
