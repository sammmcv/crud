package net.codejava;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import org.hibernate.mapping.Map;
//import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class ApiService {

    private final RestTemplate restTemplate;
    private static final String API_KEY = "3afbf06";
    private static final String API_URL = "https://www.omdbapi.com/";

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @SuppressWarnings("unchecked") // para que no salga el warning por el cambio de objeto a hashmap
    public List<HashMap<String, Object>> searchMovies(String title) { // para obtener la lista de peliculas
        String url = API_URL + "?s=" + title + "&apikey=" + API_KEY; // gets para cada peli en forma de lista
        HashMap<String, Object> response = restTemplate.getForObject(url, HashMap.class);
        return response != null && response.get("Search") != null
            ? (List<HashMap<String, Object>>) response.get("Search")
            : new ArrayList<>();
    }
    @SuppressWarnings("unchecked") // para que no salga el warning por el cambio de objeto a hashmap
    public HashMap<String, Object> getMovieByImbdId(String imdbID) { // informacion de una sola pelicula
        //URL usando el imdbID
        String url = API_URL +  "?apikey=" + API_KEY + "&i=" + imdbID;
        
        // GET para datos de la película
        return restTemplate.getForObject(url, HashMap.class);
    }
}