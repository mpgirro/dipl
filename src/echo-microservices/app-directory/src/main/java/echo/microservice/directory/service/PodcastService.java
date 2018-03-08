package echo.microservice.directory.service;

import echo.core.domain.dto.PodcastDTO;
import echo.microservice.directory.repository.PodcastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PodcastService {

    @Autowired
    private PodcastRepository podcastRepository;

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOne(Long id) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOneByEchoId(String echoId) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

}