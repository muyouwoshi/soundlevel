#pragma once

#ifdef FFT_EXPORTS
#define FFT_API
#else
#define FFT_API
#endif
#include <stdint.h>
#include "common.h"

struct FFTParam
{
	WindowType type;
	uint32_t winlen;// 采样率/频率分辨率
//	uint32_t zpad;
	uint32_t winshift;// winlen*(1-重叠率）
	double winpar;
	double samplerate;
	double range;
//	uint32_t totalSamples;
	int sample_type; // 0---int24 1---float
};

inline void InitFFtParam(FFTParam& param)
{
	param.type = HANNING;
	param.winlen = 512;//Length samples
//	param.zpad = 2;
	param.winshift = 128;//Window Shift(Samples)
	param.winpar = 0;
	param.sample_type = 0;
	param.samplerate=48000;
	param.range=93;
//	param.totalSamples=48000*2;
}

class FFT_API CFFTCalc
{
public:
	CFFTCalc(FFTParam param);
	~CFFTCalc();
	void Calculate(const int* data, int samples);
	void Calculate(const float* data, int samples);
	//before calculate return total frames for color bitmap create
	uint32_t GetResultInfo(uint32_t* channels);
	bool GetResult(uint32_t index, float* result);
	void GetMinMaxValue(float& min_value, float& max_value);
	bool GetMeanResult(float* result);
	void ResetMeanResult();
	void ResultClear();
	void Reset();
	void SetFFTParam(FFTParam param);
	FFTParam GetFFTParam();
private:
	CFFTCalc(const CFFTCalc&);
	CFFTCalc& operator = (const CFFTCalc&);
private:
	void* m_pImpl;
};
