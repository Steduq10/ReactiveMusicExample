package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.AlbumDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Album;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.ISongRepository;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class SongServiceImplTest {

    @Mock
    ISongRepository songRepositoryMock;

    ModelMapper modelMapper;

    SongServiceImpl songService;


    @BeforeEach
    void init(){
        modelMapper = new ModelMapper();
        songService = new SongServiceImpl(songRepositoryMock,modelMapper);
    }

    @Test
    @DisplayName("findAllSongs()")
    void findAllSongs() {

        ArrayList<Song> listSongs = new ArrayList<>();
        listSongs.add(new Song());
        listSongs.add(new Song());

        ArrayList<SongDTO> listSongsDTO = listSongs.stream().map(song -> modelMapper.map(song,SongDTO.class)).collect(Collectors.toCollection(ArrayList::new));

        var fluxResult = Flux.fromIterable(listSongs);
        var fluxResultDTO = Flux.fromIterable(listSongsDTO);


        ResponseEntity<Flux<SongDTO>> respEntResult = new ResponseEntity<>(fluxResultDTO, HttpStatus.FOUND);


        Mockito.when(songRepositoryMock.findAll()).thenReturn(fluxResult);


        var service = songService.findAllSongs();


        StepVerifier.create(service)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode().is3xxRedirection())
                .expectComplete().verify();

    }


    @Test
    @DisplayName("findSongById()")
    void findSongById() {
        Song songExpected = new Song();
        songExpected.setIdAlbum("12345678-9");
        songExpected.setName("songTesting1");
        songExpected.setIdSong("459df7");
        LocalTime newDuration = LocalTime.of(00,04,25);
        songExpected.setDuration(newDuration);
        songExpected.setArrangedBy("Simon Torres");
        songExpected.setLyricsBy("Laura Gonzalez");
        songExpected.setProducedBy("Pablo Alvarez");

        var songDTOExpected = modelMapper.map(songExpected,SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(songDTOExpected,HttpStatus.FOUND);

        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.just(songExpected));

        var service = songService.findSongById("12345678-9");

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete()
                .verify();


        Mockito.verify(songRepositoryMock).findById("12345678-9");
    }

    @Test
    @DisplayName("findSongByIdError()")
    void findSongByIdError() { //Not found

        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        var service = songService.findSongById("12345678-9");

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete().verify();

        Mockito.verify(songRepositoryMock).findById("12345678-9");
    }

    @Test
    @DisplayName("saveSong()")
    void saveSong(){
        Song songExpected = new Song();
        songExpected.setIdAlbum("12345678-9");
        songExpected.setName("songTesting1");
        songExpected.setIdSong("459df7");
        LocalTime newDuration = LocalTime.of(00,04,25);
        songExpected.setDuration(newDuration);
        songExpected.setArrangedBy("Simon Torres");
        songExpected.setLyricsBy("Laura Gonzalez");
        songExpected.setProducedBy("Pablo Alvarez");

        var songDTOExpected = modelMapper.map(songExpected,SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(songDTOExpected,HttpStatus.CREATED);

        Mockito.when(songRepositoryMock.save(Mockito.any(Song.class))).thenReturn(Mono.just(songExpected));

        var service = songService.saveSong(songDTOExpected);

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete()
                .verify();


        Mockito.verify(songRepositoryMock).save(songExpected);
    }

    @Test
    @DisplayName("saveSongError()")
    void saveSongError() { //Not found


        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

        Mockito.when(songRepositoryMock.save(Mockito.any(Song.class))).thenReturn(Mono.empty());
        LocalTime newDuration = LocalTime.of(00,04,25);
        var service = songService.saveSong(new SongDTO(
                "459df7",
                "songTesting1",
                "12345678-9",
                "Laura Gonzalez",
                "Pablo Alvarez",
                "Simon Torres",
                newDuration
        ));

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete().verify();

        Mockito.verify(songRepositoryMock).save(new Song(
                "459df7",
                "songTesting1",
                "12345678-9",
                "Laura Gonzalez",
                "Pablo Alvarez",
                "Simon Torres",
                newDuration
        ));
    }


    @Test
    @DisplayName("updateSong()")
    void updateSong(){
        Song songExpected = new Song();
        songExpected.setIdAlbum("12345678-9");
        songExpected.setName("songTestingUpdate1");
        songExpected.setIdSong("459df7");
        LocalTime newDuration = LocalTime.of(00,04,25);
        songExpected.setDuration(newDuration);
        songExpected.setArrangedBy("Pedro Torres");
        songExpected.setLyricsBy("Laura Gonzalez");
        songExpected.setProducedBy("Pablo Alvarez");

        var songEdited = songExpected.toBuilder().name("songTestingEdited").build();

        var songDTOEdited = modelMapper.map(songEdited,SongDTO.class);


        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(songDTOEdited,HttpStatus.ACCEPTED);

        //You need to mock the findById first and because you use it previous the save/update
        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.just(songExpected));
        Mockito.when(songRepositoryMock.save(Mockito.any(Song.class))).thenReturn(Mono.just(songEdited));

        var service = songService.updateSong("12345678-9", songDTOEdited);

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete()
                .verify();


        Mockito.verify(songRepositoryMock).save(songEdited);

    }

    @Test
    @DisplayName("updateSongError()")
    void updateSongError() { //Not found


        ResponseEntity<SongDTO> songDTOResponse = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.empty());
        Mockito.when(songRepositoryMock.save(Mockito.any(Song.class))).thenReturn(Mono.empty());
        LocalTime newDuration = LocalTime.of(00,04,25);
        var service = songService.updateSong("459df7",new SongDTO(
                "459df7",
                "songTesting1",
                "12345678-9",
                "Laura Gonzalez",
                "Pablo Alvarez",
                "Simon Torres",
                newDuration
        ));

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete().verify();

        Mockito.verify(songRepositoryMock).save(new Song(
                "459df7",
                "songTesting1",
                "12345678-9",
                "Laura Gonzalez",
                "Pablo Alvarez",
                "Simon Torres",
                newDuration
        ));
    }



    @Test
    @DisplayName("deleteSong()")
    void deleteSong(){
        Song songExpected = new Song();
        songExpected.setIdAlbum("12345678-9");
        songExpected.setName("songTestingUpdate1");
        songExpected.setIdSong("459df7");
        LocalTime newDuration = LocalTime.of(00,04,25);
        songExpected.setDuration(newDuration);
        songExpected.setArrangedBy("Pedro Torres");
        songExpected.setLyricsBy("Laura Gonzalez");
        songExpected.setProducedBy("Pablo Alvarez");

        ResponseEntity<String> responseDelete = new ResponseEntity<>(songExpected.getIdSong(),HttpStatus.ACCEPTED);

        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class)))
                .thenReturn(Mono.just(songExpected));
        Mockito.when(songRepositoryMock.deleteById(Mockito.any(String.class)))
                .thenReturn(Mono.empty());


        var service = songService.deleteSong("459df7");


        StepVerifier.create(service).expectNext(responseDelete).expectComplete().verify();

        Mockito.verify(songRepositoryMock).findById("459df7");
        Mockito.verify(songRepositoryMock).deleteById("459df7");

    }

    @Test
    @DisplayName("deleteSongError()")
    void deleteSongError() { //Not found

        ResponseEntity<String> songDTOResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);


        Mockito.when(songRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.empty());


        Mockito.when(songRepositoryMock.deleteById(Mockito.any(String.class)))
                .thenReturn(Mono.empty());

        var service = songService.deleteSong("12345678-9");

        StepVerifier.create(service)
                .expectNext(songDTOResponse)
                .expectComplete().verify();

        Mockito.verify(songRepositoryMock).findById("12345678-9");
        Mockito.verify(songRepositoryMock).deleteById("12345678-9");
    }

}
