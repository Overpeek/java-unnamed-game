package logic;

import utility.SimplexNoise_octave;

public class Noisemaps {

	public SimplexNoise_octave mapnoise;
	public SimplexNoise_octave biomenoise1;
	public SimplexNoise_octave biomenoise2;
	public SimplexNoise_octave plantnoise1;
	public SimplexNoise_octave plantnoise2;
	
	public Noisemaps(int seed) {

		mapnoise = new SimplexNoise_octave(seed);
		mapnoise.setFreq(Settings.MAP_FREQ);
		biomenoise1 = new SimplexNoise_octave(seed + 1);
		biomenoise1.setFreq(Settings.MAP_BIOME_FREQ);
		biomenoise2 = new SimplexNoise_octave(seed + 2);
		biomenoise2.setFreq(Settings.MAP_BIOME_FREQ);
		plantnoise1 = new SimplexNoise_octave(seed + 3);
		plantnoise1.setFreq(Settings.MAP_PLANT1_FREQ);
		plantnoise2 = new SimplexNoise_octave(seed + 4);
		plantnoise2.setFreq(Settings.MAP_PLANT2_FREQ);
		
	}
	
}
