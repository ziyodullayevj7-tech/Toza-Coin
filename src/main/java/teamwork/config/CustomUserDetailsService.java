package teamwork.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamwork.entity.ProfileEntity;
import teamwork.repository.ProfileRepository;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private ProfileRepository profileRepository;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(username);
        if (optional.isEmpty()){
            throw new UsernameNotFoundException(username);
        }
        return new CustomUserDetails(optional.get());
    }
}
