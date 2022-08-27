package ec.com.reactive.music.web;

import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.service.impl.PlayListServiceImpl;
import ec.com.reactive.music.service.impl.SongServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
public class PlayListResource {

    @Autowired
    private PlayListServiceImpl playListService;

    private SongServiceImpl songService;

    //GET
    @GetMapping("/findPlayList/{id}")
    private Mono<ResponseEntity<PlaylistDTO>> getAlbumById(@PathVariable String id){
        return playListService.findPlayListById(id);
    }

    @GetMapping("/findAllPlayList")
    private Mono<ResponseEntity<Flux<PlaylistDTO>>> findAllAlbums(){
        return playListService.findAllPlayList();
    }

    @PostMapping("save/PlayList")
    public Mono<ResponseEntity<PlaylistDTO>> savePlayList(@RequestBody PlaylistDTO playlistDTO){
        return playListService.savePlayList(playlistDTO);
    }

    @PutMapping("update/PlayList/{id}")
    public Mono<ResponseEntity<PlaylistDTO>> updateAlbum(@PathVariable String id, @RequestBody PlaylistDTO playlistDTO){
        return playListService.updatePlayList(id, playlistDTO);
    }

    @PutMapping("/addPlayListSong/{idPlayList}/{idSong}")
    private Mono<ResponseEntity<PlaylistDTO>> addSongPlayList(@PathVariable String idPlayList, @PathVariable String idSong){
        return songService.findSongById(idSong)
                .flatMap(songDTOResponseEntity -> songDTOResponseEntity.getStatusCode().is4xxClientError() ?
                        playListService.addSong("SONGNOEXIST", new SongDTO()): playListService.addSong(idPlayList, songDTOResponseEntity.getBody()));
                //.flatMap(songDTOResponseEntity ->  playListService.addSong(idPlayList, songDTOResponseEntity.getBody()));
    }

    @PutMapping("/deletePlayListSong/{idPlayList}/{idSong}")
    private Mono<ResponseEntity<PlaylistDTO>> deleteSongPlayList(@PathVariable String idPlayList, @PathVariable String idSong){
        return songService.findSongById(idSong)
                .flatMap(songDTOResponseEntity -> songDTOResponseEntity.getStatusCode().is4xxClientError() ?
                        playListService.addSong("SONGNOEXIST", new SongDTO()): playListService.deleteSong(idPlayList, songDTOResponseEntity.getBody()));
                //.flatMap(songDTOResponseEntity ->  playListService.deleteSong(idPlayList, songDTOResponseEntity.getBody()));
    }

    @DeleteMapping("delete/PlayList/{id}")
    public Mono< ResponseEntity <Void>> deletePlayList(@PathVariable String id){
        return  playListService.deletePlayList(id);
    }
}
