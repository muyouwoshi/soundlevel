#pragma once
#include <Common.h>

void GetFFTWeightingResult(double samplerate, float* data, int count, FreqWeighting weighting);
void GetOctaveWeightingResult(ThirdOctaveType type, float* data, FreqWeighting weighting);
