#include <Level.h>
#include "AddWeighting.h"
#include <math.h>

void GetFFTWeightingResult(double samplerate, float* data, int count, FreqWeighting weighting)
{
	if (weighting == NoWeighting)
		return;
	double deltaFreq = samplerate / (2 * (count - 1));
	data[0] += (float)(20 * log10(GetAbcMagnitude(deltaFreq, weighting)));
	for (int i = 1; i != count; ++i)
	{
		data[i] += (float)(20 * log10(GetAbcMagnitude(deltaFreq * i, weighting)));
	}
}

void GetOctaveWeightingResult(ThirdOctaveType type, float* data, FreqWeighting weighting)
{
	if (weighting == NoWeighting)
		return;
	int nChannel = 30;
	double factor, freq;
	factor = pow(2.0, -1 / 3.0);
	if (type == TOCT_TYPE_ANSI_MAIN || type == TOCT_TYPE_IEC_MAIN)
	{
		freq = 16000;
	}
	else if (type == TOCT_TYPE_IEC_SIDE)
	{
		freq = 1000.0 * pow(2.0, 11.5 / 3.0);
	}
	for (int i = nChannel - 1; i >= 0; --i)
	{
		data[i] += (float)(20 * log10(GetAbcMagnitude(freq, weighting)));
		freq *= factor;
	}
}
