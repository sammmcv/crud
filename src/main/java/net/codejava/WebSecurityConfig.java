// establecemos la seguridad implementada en la aplicacion, roles, autorizaciones e inicio de sesion.
package net.codejava;

//import javax.sql.DataSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity // usamos websecurity de spring
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //@Autowired
    //private DataSource dataSource;

    @Bean // resttemplate para el API 
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean // con esto podemos trabajar con roles y credenciales
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean // definicion del codificador
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // autenticacion de los usuarios al iniciar sesion
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); // autenticador
        authProvider.setUserDetailsService(userDetailsService()); // detalles del usuario
        authProvider.setPasswordEncoder(passwordEncoder()); // codificador
        return authProvider;
    }

    @Override // para usar el authprovider de arriba
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override // permisos de entrada y redirecciones
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeRequests(auth -> auth
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/users").hasRole("ADMIN")
                .antMatchers("/consumeApi").hasRole("ADMIN")
                .antMatchers("/welcome").authenticated()
                .antMatchers("/api").authenticated()
                .antMatchers("/consumeApi").authenticated()
                .antMatchers("/movie/**").hasRole("ADMIN")
                .antMatchers("/editUser/**").hasRole("ADMIN")
                .antMatchers("/login").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")//contraseña incorrecta
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/welcome", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
    }
}