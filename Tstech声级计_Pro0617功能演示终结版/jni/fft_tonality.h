#pragma once
#ifdef FFT_EXPORTS
#define FFT_API
#else
#define FFT_API
#endif
#include <functional>

class CFFTCalcImpl;
class FFT_API CTonalityFFT
{
public:
	CTonalityFFT(void* pImpl) : m_pImpl(static_cast<CFFTCalcImpl*>(pImpl))
	{
	}
	double GetMinDeltaL(double cbw);
	void SetResultProcessFunc(std::function<void(double*)> func);
	double GetDataPref();
	double GetScalePower();
	double GetScaleMagnitude();
private:
	CTonalityFFT(const CTonalityFFT&);
	CTonalityFFT& operator = (const CTonalityFFT&);
private:
	CFFTCalcImpl* m_pImpl;
};
