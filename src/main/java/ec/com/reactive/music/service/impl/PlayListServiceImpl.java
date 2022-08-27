package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.IPlayListRepository;
import ec.com.reactive.music.repository.ISongRepository;
import ec.com.reactive.music.service.IPlayListService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.ArrayList;

@Service
@AllArgsConstructor

public class PlayListServiceImpl implements IPlayListService {
    @Autowired
    private IPlayListRepository iplayListRepository;

    @Autowired
    private ISongRepository iSongRepository;
    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Mono<ResponseEntity<Flux<PlaylistDTO>>> findAllPlayList() {
        return Mono.justOrEmpty(new ResponseEntity<>(this.iplayListRepository
                        .findAll()
                        .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NO_CONTENT.toString())))
                        .map(this::entityToDTO),HttpStatus.FOUND))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> findPlayListById(String id) {
        return this.iplayListRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .map(this::entityToDTO)
                .map(playlistDTO  -> new ResponseEntity<>(playlistDTO, HttpStatus.FOUND))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> savePlayList(PlaylistDTO playlistDTO) {
        return this.iplayListRepository
                .save(DTOToEntity(playlistDTO))
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NO_CONTENT.toString())))
                .map(this::entityToDTO)
                .map(playlistDTO01 -> new ResponseEntity<>(playlistDTO01, HttpStatus.CREATED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT)));

    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> updatePlayList(String id, PlaylistDTO playlistDTO) {
        playlistDTO.setIdPlaylist(id);
        return iplayListRepository.findById(id).switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .map(playlist -> DTOToEntity(playlistDTO))
                .flatMap(iplayListRepository::save)
                .map(this::entityToDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePlayList(String id) {
        return this.iplayListRepository
                .findById(id).flatMap(album -> iplayListRepository.delete(album).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> addSong(String id, SongDTO songDTO) {

        return this.iplayListRepository.findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> {
                    var songs = playlist.getSongs();
                    var newSong = DTOtoSong(songDTO);
                    songs.add(newSong);

                    playlist.setSongs(songs);
                    playlist.setDuration(playlist.getDuration().plusMinutes(newSong.getDuration().getMinute()).plusSeconds(newSong.getDuration().getSecond()));

                    return this.savePlayList(entityToDTO(playlist));
                })
                .map(playlistDTO01 -> new ResponseEntity<>(playlistDTO01.getBody(), HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));


    }

    @Override
    public Mono<ResponseEntity<PlaylistDTO>> deleteSong(String id, SongDTO songDTO) {
        return this.iplayListRepository.findById(id)
                .switchIfEmpty(Mono.error(new Throwable(HttpStatus.NOT_FOUND.toString())))
                .flatMap(playlist -> {
                    var songs = playlist.getSongs();
                    var oldSong = DTOtoSong(songDTO);
                    var i = songs.indexOf(oldSong);

                    songs.remove(i);

                    playlist.setSongs(songs);
                    playlist.setDuration(playlist.getDuration().minusMinutes(oldSong.getDuration().getMinute()).minusSeconds(oldSong.getDuration().getSecond()));

                    return this.savePlayList(entityToDTO(playlist));
                })
                .map(playlistDTO01 -> new ResponseEntity<>(playlistDTO01.getBody(), HttpStatus.ACCEPTED))
                .onErrorResume(throwable -> Mono.just(new ResponseEntity<>(HttpStatus.NOT_MODIFIED)));
    }

    public Song DTOtoSong(SongDTO songDTO) {
        return this.modelMapper.map(songDTO,Song.class);
    }

    @Override
    public Playlist DTOToEntity(PlaylistDTO playlistDTO) {
        return this.modelMapper.map(playlistDTO, Playlist.class);
    }

    @Override
    public PlaylistDTO entityToDTO(Playlist playlist) {
        return this.modelMapper.map(playlist, PlaylistDTO.class);
    }

}
