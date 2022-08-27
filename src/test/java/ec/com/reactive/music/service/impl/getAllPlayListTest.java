package ec.com.reactive.music.service.impl;


import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.repository.IPlayListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class getAllPlayListTest {

    @Mock
    IPlayListRepository playListRepositoryMock;

    ModelMapper modelMapper;


    PlayListServiceImpl playListService;

    @BeforeEach
    void init(){
        modelMapper = new ModelMapper();
        playListService = new PlayListServiceImpl(playListRepositoryMock,modelMapper);
    }

    @Test
    @DisplayName("findAllPlayList()")
    void findAllPlayList() {

        ArrayList<Playlist> listPlayList = new ArrayList<>();
        listPlayList.add(new Playlist());
        listPlayList.add(new Playlist());

        ArrayList<PlaylistDTO> listPlaylistDTO = listPlayList.stream().map(playlist -> modelMapper.map(playlist,PlaylistDTO.class)).collect(Collectors.toCollection(ArrayList::new));

        var fluxResult = Flux.fromIterable(listPlayList);
        var fluxResultDTO = Flux.fromIterable(listPlaylistDTO);


        ResponseEntity<Flux<PlaylistDTO>> respEntResult = new ResponseEntity<>(fluxResultDTO, HttpStatus.FOUND);


        Mockito.when(playListRepositoryMock.findAll()).thenReturn(fluxResult);


        var service = playListService.findAllPlayList();


        StepVerifier.create(service)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode().is3xxRedirection())
                .expectComplete().verify();



    }

}
