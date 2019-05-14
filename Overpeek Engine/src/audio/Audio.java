package audio;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.stb.*;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL.*;
import static org.lwjgl.system.MemoryStack.*;

public class Audio {

	int audioSource;
	int buffer;
	
	static long device;
	static long context;
	static boolean initialized = false;
	
	public static void init() {
		if (initialized) return;
		initialized = true;
		
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		device = alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);
		
		ALCCapabilities alcCapabilities = createCapabilities(device);
		ALCapabilities alCapabilities = createCapabilities(alcCapabilities);
		
		if(alCapabilities.OpenAL10) {
		    //OpenAL 1.0 is supported
		}
	}
	
	public static void clean() {
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
	public void close() {
		alDeleteBuffers(buffer);
	}
	  
	//Only .ogg files
	public static Audio loadAudio(String path) {
		if (!initialized) init();
		
		Audio returned = new Audio();
		
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);

		ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
		
		//Retrieve the extra information that was stored in the buffers by the function
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		//Free the space we allocated earlier
		stackPop();
		stackPop();
		
		//Find the correct OpenAL format
		int format = -1;
		if(channels == 1) {
		    format = AL_FORMAT_MONO16;
		} else if(channels == 2) {
		    format = AL_FORMAT_STEREO16;
		}
		
		
		returned.buffer = alGenBuffers();
		alBufferData(returned.buffer, format, rawAudioBuffer, sampleRate);
		
		rawAudioBuffer.clear();
		
		returned.audioSource = alGenSources();
		alSourcei(returned.audioSource, AL_BUFFER, returned.buffer);
		
		return returned;
	}
	
	public void play() {
		alSourcePlay(audioSource);
	}
	
}
