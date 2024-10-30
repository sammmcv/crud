package net.codejava;

import java.util.List;
//import java.security.Principal;
//import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.hibernate.mapping.Map;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("") // redireccion a la pagina de inicio
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/register") // redireccion a la pagina de registro
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // mandamos un modelo de usuario al formulario de registro
        return "signup_form";
    }

    @GetMapping("/login") // redireccion login
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // para codificar
        String encodedPassword = passwordEncoder.encode(user.getPassword()); // obtenemos la contraseña
        user.setPassword(encodedPassword); // la codificamos

        user.setRole("USER");  // al crearse es USER por default
        user.setEnabled(true);  // usuario habilitado por default

        userRepo.save(user); //guardamos el usuario en la bd

        return "register_success"; // si fue exitoso
    }

    @GetMapping("/users") // redireccion a la pagina de usuarios (solo admins)
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll(); // para ver todos los users en la bd
        model.addAttribute("listUsers", listUsers); // de aqui podra sacar para todos los users
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fullName = userDetails.getFullName(); // obtenemos tood desde customuserdetails
        model.addAttribute("fullName", fullName);  // pasar el nombre completo al modelo, vista html
        return "users";
    }

    @GetMapping("/deleteUser/{id}")
    public String confirmDeleteUser(@PathVariable("id") Long id, Model model) {
        User user = userRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id)); // exepcion
        model.addAttribute("user", user);  // pasar el usuario al modelo
        return "confirm_delete_user";  // redirige a la página de confirmación
    }

    @PostMapping("/updateUserRole/{id}")
    public String updateUserRole(@PathVariable("id") Long id, @RequestParam("role") String newRole) {
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("id del user invalido:" + id));

            user.setRole(newRole); //cambio de rol
            userRepo.save(user); // guardamos cambios
            return "redirect:/welcome"; // redireccion
        } catch (Exception e) { //exepcion
            return "error";
        }
    }

    @PostMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepo.deleteById(id);  // eliminar el usuario
        return "redirect:/users";  // redirige de nuevo a la lista de usuarios
    }

    @GetMapping("/editUser/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
    User user = userRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    model.addAttribute("user", user);  // pasar el usuario al modelo
    return "edit_user";  // redirige a la página de edición
    }

    @PostMapping("/editUser/{id}")
    public String updateUser(@PathVariable("id") Long id, User userDetails) {
        User user = userRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        user.setFirstName(userDetails.getFirstName()); 
        user.setLastName(userDetails.getLastName()); //actualizar datos
        user.setEmail(userDetails.getEmail());

        userRepo.save(user);  // guardar el usuario actualizado
        return "redirect:/users";  // redirigir de nuevo a la lista de usuarios
    }

    @GetMapping("/welcome")
    public String showWelcomePage(Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fullName = userDetails.getFullName(); // obtenemos desde customuserdetails
        String email = userDetails.getUsername();  // el correo es el nombre de usuario
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        return "welcome";  // redirige a la página welcome.html tras login exitoso
    }
}
