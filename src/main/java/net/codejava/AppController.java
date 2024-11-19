package net.codejava;

import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
//import java.security.Principal;
//import java.util.Map;
//import org.hibernate.mapping.Map;
//import org.springframework.data.crossstore.HashMapChangeSet;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.hibernate.mapping.Map;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class AppController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/consumeApi")
        public String showApiPage() {
            return "api_view";
        }
        
    @GetMapping("/api")
        public String searchMovies(@RequestParam("title") String title, Model model) {
            List<HashMap<String, Object>> movieData = apiService.searchMovies(title);
            model.addAttribute("movieData", movieData); // pasar la lista al modelo api view
            return "api_view";
        }

    @GetMapping("/movie/details/{id}")
        public String getMovieDetails(@PathVariable("id") String imdbID, Model model) {
            HashMap<String, Object> movieDetails = apiService.getMovieByImbdId(imdbID);
            model.addAttribute("movieDetails", movieDetails);
            return "movie_details"; // vista con los detalles de la película individual
        }
        
    @Autowired
    private UserRepository userRepo;
    
    @GetMapping("") // redireccion a la pagina de inicio (sin iniciar sesion)
        public String viewHomePage() {
            return "index";
        }

    @GetMapping("/register") // redireccion a la pagina de registro
        public String showRegistrationForm(Model model) {
            model.addAttribute("user", new User()); // mandamos un modelo de usuario al formulario de registro
            return "signup_form";
        }

    @GetMapping("/login")
        public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
            if (error != null) {
                model.addAttribute("loginError", "User o password incorrectos, intenta de nuevo");
            }
            return "login";
        }

    @PostMapping("/process_register") // redireccion a la pagina de registro
        public String processRegister(User user, @RequestParam("profilePicture") MultipartFile imageFile) { // para la foto de perfil
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // encoder de la contra
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setRole("USER"); // user por default
            user.setEnabled(true); // cuenta activada por default

            try {
                if (!imageFile.isEmpty()) {
                    user.setPhoto(imageFile.getBytes()); // guarda la imagen sin compresión
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            userRepo.save(user);
            return "register_success";
        }

        @GetMapping("/welcome")
            public String showWelcomePage(Model model) {
                CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String email = userDetails.getUsername();
                User user = userRepo.findByEmail(email);
    
                String photoBase64 = user.getPhoto() != null ? Base64.getEncoder().encodeToString(user.getPhoto()) : null;
                model.addAttribute("photoBase64", photoBase64);
    
                model.addAttribute("fullName", userDetails.getFullName());
                model.addAttribute("email", email);
                return "welcome";
            }

    @GetMapping("/users") // redireccion a la pagina de usuarios (solo admins)
        public String listUsers(Model model) {
            List<User> listUsers = userRepo.findAll(); // para ver todos los users en la bd

            listUsers.forEach(user -> { // por cada usuario obtenemos la pfp
                if (user.getPhoto() != null) {
                    String photoBase64 = Base64.getEncoder().encodeToString(user.getPhoto());
                    user.setPhotoBase64(photoBase64); // usamos el getter para photoBase64
                }
            });

            model.addAttribute("listUsers", listUsers); // de aqui podra sacar para todos los users
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String fullName = userDetails.getFullName(); // obtenemos tood desde customuserdetails
            model.addAttribute("fullName", fullName);  // pasar el nombre completo al modelo, vista html
            return "users";
        }

    @PostMapping("/updateUserRole/{id}") // tampoco hay pagina para actualizar usuarios
        public String updateUserRole(@PathVariable("id") Long id, @RequestParam("role") String newRole) {
            try {
                User user = userRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
                user.setRole(newRole);
                userRepo.save(user);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // si el user cambia su rol, actualiza
                if (authentication.getName().equals(user.getEmail())) {
                    CustomUserDetails updatedUserDetails = new CustomUserDetails(user);
                    Authentication newAuth = new UsernamePasswordAuthenticationToken( // nueva autenticacion debido a la actualizacion
                            updatedUserDetails,
                            authentication.getCredentials(),
                            updatedUserDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
                return "redirect:/welcome"; // redireccion a la página de bienvenida
            } catch (Exception e) {
                return "error";
            }
        }

    @PostMapping("/deleteUser/{id}") // no hay pagina
        public String deleteUser(@PathVariable("id") Long id) {
            userRepo.deleteById(id);  // eliminar el usuario
            return "redirect:/users";  // redirige de nuevo a la lista de usuarios
        }

    @GetMapping("/editUser/{id}")
        public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

                    if (user.getPhoto() != null) {
                        String photoBase64 = Base64.getEncoder().encodeToString(user.getPhoto());
                        model.addAttribute("photoBase64", photoBase64);
                    }

        model.addAttribute("user", user);  // pasar el usuario al modelo
        return "edit_user";  // redirige a la página de edición
        }

    @PostMapping("/editUser/{id}")
        public String updateUser(@PathVariable("id") Long id,
                                User userDetails,
                                @RequestParam("profilePicture") MultipartFile imageFile) {
            User user = userRepo.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

            // Actualiza los datos del usuario
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setEmail(userDetails.getEmail());

            // Si se ha cargado una nueva foto, actualízala
            try {
                if (!imageFile.isEmpty()) {
                    user.setPhoto(imageFile.getBytes());  // guarda la imagen sin compresión
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            userRepo.save(user);  // Guarda el usuario actualizado
            return "redirect:/users";  // Redirige de nuevo a la lista de usuarios
        }

    @PostMapping("/update_photo")
        public String updatePhoto(@RequestParam("profilePicture") MultipartFile imageFile) {
            // obtiene el usuario autenticado
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = userDetails.getUsername();

            // busca al usuario en la base de datos
            User user = userRepo.findByEmail(email);

            try {
                // si se ha subido una nueva imagen, actualiza el campo photo sin compresión
                if (!imageFile.isEmpty()) {
                    user.setPhoto(imageFile.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            userRepo.save(user);
            return "redirect:/welcome"; // redirige de nuevo a la página de bienvenida
        }
    }
