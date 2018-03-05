package echo.microservice.directory.service;

import echo.core.domain.dto.PodcastDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PodcastService {

    @Transactional
    public Optional<PodcastDTO> findOne(Long id) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    @Transactional
    public Optional<PodcastDTO> findOneByEchoId(String echoId) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

}
