package audio;

import static org.lwjgl.openal.AL.createCapabilities;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.ALC.createCapabilities;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.stb.STBVorbis;

import utility.DataIO;

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
	public static Audio loadAudio(String path) throws FileNotFoundException {
		if (!initialized) init();
		
		Audio returned = new Audio();
		
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);

		//Read all data from inputstream to bytebuffer
		ByteBuffer audiodatabuffer = DataIO.readResourceFile(path);
		
		
		//Decode
		ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(audiodatabuffer, channelsBuffer, sampleRateBuffer);
		//if (rawAudioBuffer == null) Logger.error("Couldn't load audiofile: " + path);
		
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
