package com.creativemd.voicechat.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.creativemd.voicechat.core.VoiceChat;

public class AudioConsumer {

	public static long STARTED_AT; // hope thread safety wont cause problems
	// otherwise make it a atomiclong but that''s probabl,y overkill
	public static int CHANNELS = VoiceChat.format.getChannels();
	public static int SAMPLE_RATE = (int) VoiceChat.format
			.getSampleRate();
	public static int NUM_PRODUCERS = 1;
	public static int BUFFER_SIZE_FRAMES = (int) (VoiceChat.format
			.getSampleRate() / 4);

	// audio block with "when to play" tag
	private static class QueuedBlock {

		final long when;
		final short[] data;

		public QueuedBlock(long when, short[] data) {
			this.when = when;
			this.data = data;
		}
	}

	// need not normally be so low but in this example
	// we're mixing down a bunch of full scale sinewaves
	private static final double MIXDOWN_VOLUME = 1.0 / NUM_PRODUCERS;

	private final List<QueuedBlock> finished = new ArrayList<QueuedBlock>();
	private final short[] mixBuffer = new short[BUFFER_SIZE_FRAMES * CHANNELS];
	private final byte[] audioBuffer = new byte[BUFFER_SIZE_FRAMES * CHANNELS
			* 2];

	private final PlayThread thread;
	private final AtomicLong position = new AtomicLong();
	private final AtomicBoolean running = new AtomicBoolean(true);
	private final ConcurrentLinkedQueue<QueuedBlock> scheduledBlocks = new ConcurrentLinkedQueue<QueuedBlock>();

	public AudioConsumer() {
		thread = new PlayThread(this);
	}

	public void start() {
		thread.start();
	}
	
	public void close()
	{
		thread.interrupt();
		stop();
	}

	public void stop() {
		running.set(false);
	}

	// gets the play cursor. note - this is not accurate and
	// must only be used to schedule blocks relative to other blocks
	// (e.g., for splitting up continuous sounds into multiple blocks)
	public long position() {
		return position.get();
	}

	// put copy of audio block into queue so we don't
	// have to worry about caller messing with it afterwards
	public void mix(long when, short[] block) {
		scheduledBlocks.add(new QueuedBlock(when, Arrays.copyOf(block,
				block.length)));
	}

	// better hope mixer 0, line 0 is output
	public void run() {
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);
		try {
			Line.Info[] lineInfo = mixer.getSourceLineInfo();
			SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo[0]);
			try {
				line.open(VoiceChat.format, VoiceChat.format.getFrameSize()*BUFFER_SIZE_FRAMES);
				line.start();
				while (running.get())
					processSingleBuffer(line);
				line.stop();
			} finally {

			}
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		}
	}

	// mix down single buffer and offer to the audio device
	private void processSingleBuffer(SourceDataLine line) {

		if(position.get()==0)
			STARTED_AT = System.currentTimeMillis();

		Arrays.fill(mixBuffer, (short) 0);
		
		/*ThreadLocalRandom rand = ThreadLocalRandom.current();
        for(int i = 0; i < VoiceChat.format.getChannels()*BUFFER_SIZE_FRAMES; i++)
        	mixBuffer[i] = (short)(rand.nextDouble() * Short.MAX_VALUE*0.2);*/
        
		long bufferStartAt = position.get();

		// mixdown audio blocks
		for (QueuedBlock block : scheduledBlocks) {

			int blockFrames = block.data.length / CHANNELS;

			// block fully played - mark for deletion
			if (block.when + blockFrames <= bufferStartAt) {
				finished.add(block);
				continue;
			}

			// block starts after end of current buffer
			if (bufferStartAt + BUFFER_SIZE_FRAMES <= block.when)
				continue;

			// mix in part of the block which overlaps current buffer
			// note that block may have already started in the past
			// but extends into the current buffer, or that it starts
			// in the future but before the end of the current buffer
			int blockOffset = Math.max(0, (int) (bufferStartAt - block.when));
			int blockMaxFrames = blockFrames - blockOffset;
			int bufferOffset = Math.max(0, (int) (block.when - bufferStartAt));
			int bufferMaxFrames = BUFFER_SIZE_FRAMES - bufferOffset;
			for (int f = 0; f < blockMaxFrames && f < bufferMaxFrames; f++)
				for (int c = 0; c < CHANNELS; c++) {
					int bufferIndex = (bufferOffset + f) * CHANNELS + c;
					int blockIndex = (blockOffset + f) * CHANNELS + c;
					mixBuffer[bufferIndex] += (short) (block.data[blockIndex] * MIXDOWN_VOLUME);
				}
		}

		scheduledBlocks.removeAll(finished);
		finished.clear();
		ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN)
				.asShortBuffer().put(mixBuffer);
		line.write(audioBuffer, 0, audioBuffer.length);
		position.addAndGet(BUFFER_SIZE_FRAMES);
	}
}