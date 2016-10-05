#pragma once
#include "ThirdOctave.h"

#ifdef ARTICULATIONINDEX_EXPORTS
#define AI_API
#else
#define AI_API
#endif

const int NB_BAND_AI = 16;
enum AIType
{
	AI_ANSI,
	AI_UNIKELLER,
	AI_NVH,
};

//for AI do not set ThirdOctave type to TOCT_TYPE_IEC_ALL and TOCT_TYPE_ANSI_ALL 

struct AIFrame
{
	float ai[NB_BAND_AI];
};
class AI_API CAICalc
{
public:
	CAICalc(ThirdOctaveParam param, AIType type);
	~CAICalc();
	void Calculate(const int* data, uint32_t samples);
	const AIFrame* GetFrames(uint32_t* count);
	const float* Ai_time(uint32_t* count);
	double GetAI();
	void ResetResult();
private:
	CAICalc(const CAICalc&);
	CAICalc& operator = (const CAICalc&);
private:
	void* m_pImpl;
};
