package com.visearch.controller;

import com.visearch.model.JsonBody;
import com.visearch.service.TorrentDownloadService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class MainController {
    private static final Log logger = LogFactory.getLog(MainController.class);

    @Autowired
    private TorrentDownloadService torrentDownloadService;

    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public ResponseEntity<?> processRequest(@RequestBody @Valid JsonBody jsonBody) {
        String magnetLink = jsonBody.getMagnetLink();

        try {
            logger.info("Got a request with param 'magnet_link':" + magnetLink);
            torrentDownloadService.processDownloadingByMagnetLink(magnetLink);

        } catch (IllegalArgumentException e) {
            logger.error("Got IAException: ", e);
            return new ResponseEntity<>("Not null 'magnet' parameter required, " +
                    "try again after setting one.", HttpStatus.BAD_REQUEST);

        } //todo: think of session being interrupted
        return new ResponseEntity<>("\nDownloaded torrent from link: \n" + magnetLink,
                HttpStatus.OK);

    }
}

//curl -d '{"magnetLink":"magnet:?xt=urn:btih:a72a05ffb055b0c12d034dd3718a001aab1b6ab5&dn=%D0%9A%D1%80%D0%B8%D1%81%D1%82%D0%BE%D1%84%D0%B5%D1%80%20%D0%9F%D1%80%D0%B8%D1%81%D1%82&tr=http%3A%2F%2Fretracker.local%2Fannounce&tr=http%3A%2F%2Ftracker.filetracker.pl%3A8089%2Fannounce&tr=http%3A%2F%2Ftracker2.wasabii.com.tw%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.grepler.com%3A6969%2Fannounce&tr=http%3A%2F%2F125.227.35.196%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.tiny-vps.com%3A6969%2Fannounce&tr=http%3A%2F%2F87.248.186.252%3A8080%2Fannounce&tr=http%3A%2F%2F210.244.71.25%3A6969%2Fannounce&tr=http%3A%2F%2F46.4.109.148%3A6969%2Fannounce&tr=udp%3A%2F%2F46.148.18.250%3A2710&tr=http%3A%2F%2Ftracker.dler.org%3A6969%2Fannounce&tr=udp%3A%2F%2F%5B2001%3A67c%3A28f8%3A92%3A%3A1111%3A1%5D%3A2710&tr=udp%3A%2F%2Fipv6.leechers-paradise.org%3A6969"}' -H "Content-Type: application/json" -X POST http://localhost:8080/download
