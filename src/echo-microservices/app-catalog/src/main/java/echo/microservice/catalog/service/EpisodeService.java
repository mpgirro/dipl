package echo.microservice.catalog.service;

import echo.core.domain.dto.EpisodeDTO;
import echo.microservice.catalog.repository.EpisodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class EpisodeService {

    @Autowired
    private EpisodeRepository episodeRepository;

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOne(Long id) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOneByEchoId(String echoId) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

}
