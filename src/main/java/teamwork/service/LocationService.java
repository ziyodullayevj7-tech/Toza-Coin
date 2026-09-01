package teamwork.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import teamwork.dto.location.LocationDto;
import teamwork.entity.LocationEntity;
import teamwork.repository.LocationRepository;

import java.util.Optional;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public Optional<LocationEntity> saveForWasteReport(LocationDto dto) {
        LocationEntity location = new LocationEntity();
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setRegion(dto.getRegion());
        location.setDistrict(dto.getDistrict());
        location.setStreetAddress(dto.getStreetAddress());

        locationRepository.save(location);
        return Optional.of(location);
    }
}
