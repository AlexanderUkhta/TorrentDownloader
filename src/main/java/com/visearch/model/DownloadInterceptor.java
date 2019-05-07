package com.visearch.model;

import com.visearch.service.TorrentDownloadService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.visearch.config.ConfigConstantsAndMethods.MAGNET_LINK_IN_PROCESS;

@Component
public class DownloadInterceptor implements HandlerInterceptor {
    private static final Log logger = LogFactory.getLog(DownloadInterceptor.class);

    @Autowired
    private TorrentDownloadService torrentDownloadService;

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        String magnetLink = torrentDownloadService.getNextInitializedLink();
        torrentDownloadService.changeMagnetLinkStatus(magnetLink, MAGNET_LINK_IN_PROCESS);

        logger.info("Going to start processing download from new source, magnet_link = " + magnetLink);
        torrentDownloadService.processDownloadingByMagnetLink(magnetLink);

    }
}
