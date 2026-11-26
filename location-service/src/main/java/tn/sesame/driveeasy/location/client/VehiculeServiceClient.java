package tn.sesame.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "vehicule-service")
public interface VehiculeServiceClient {

    @GetMapping("/vehicules/{id}")
    VehiculeRemoteDTO getVehiculeById(@PathVariable("id") Long id);

    @PatchMapping("/vehicules/{id}/statut")
    VehiculeRemoteDTO updateStatut(@PathVariable("id") Long id, @RequestBody Map<String, String> body);
}
