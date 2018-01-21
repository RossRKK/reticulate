package poafs.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.media.Media;

public class VideoPlayer {
	 private final JFrame frame;

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	
	public VideoPlayer(InputStream video) {
		frame = new JFrame("My First Media Player");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(true);
        
        Media media = new PoafsStreamMedia(video);
        
        mediaPlayerComponent.getMediaPlayer().playMedia(media);
	}
}
