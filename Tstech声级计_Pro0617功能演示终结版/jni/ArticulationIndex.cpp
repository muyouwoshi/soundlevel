// ArticulationIndex.cpp : Defines the exported functions for the DLL application.
//

#include "ArticulationIndex.h"
#include "AIInterface.h"
#include <functional>
#include <numeric>
#include <vector>
#include <algorithm>
#include <assert.h>
#include "MeanMax.h"
#include <android/log.h>
#define LOG_TAG "debug"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)
using namespace std;

const int DEB_BAND_AI = 10;
const double k_102_factor_ai_ANSI = (0.2 / 32767);
const double k_102_factor_ai_NVH = (12.0 / 32767);
const double k_102_factor_ai_Unikeller = (40.0 / 32767);

const double k_l02_factor_l = 0.0061035156250; // (1.0/163.84) // Lin

const int A_ANSI = 12;
const int N_ANSI = 30;
const int A_Unikeller = 0;
const int N_Unikeller = 100;
const int A_NVH = 30;
const int N_NVH = 30;

static double W_ANSI[NB_BAND_AI] =
{
	0.012, 0.03, 0.03, 0.042, 0.042, 0.06, 0.06, 0.072, 0.09, 0.111, 0.114, 0.102, 0.072, 0.072, 0.06, 0
};

static int Vref_ANSI[NB_BAND_AI] =
{
	64, 68, 71, 73, 75, 75, 75, 74, 72, 70, 67, 65, 63, 60, 53, 50
};

static double W_Unikeller[NB_BAND_AI] =
{
	3.33, 6.67, 10.83, 14.17, 15, 17.5, 21.67, 24.17, 28.33, 38.33, 36.67, 31.67, 30, 25.83, 20.83, 8.33
};

static int Vref_Unikeller[NB_BAND_AI] =
{
	64, 69, 71, 73, 75, 75, 75, 74, 72, 70, 67, 65, 63, 60, 56, 51
};

static double W_NVH[NB_BAND_AI] =
{
	1, 2, 3.25, 4.25, 4.5, 5.25, 6.5, 7.25, 8.5, 11.55, 11, 9.5, 9, 7.75, 6.25, 2.5
};

static int Vref_NVH[NB_BAND_AI] =
{
	34, 39, 41, 43, 45, 45, 45, 44, 42, 40, 37, 35, 33, 30, 26, 30
};


struct Coef
{
	double A;
	double N;
	double k_102_factor_ai;
	int *VRef;
	double *W;
};

void CoefType(AIType type, Coef &c)		// static
{
	switch (type)
	{
	case AI_ANSI:	// norme ANSI
		c.N = N_ANSI;
		c.A = A_ANSI;
		c.k_102_factor_ai = k_102_factor_ai_ANSI;
		c.VRef = Vref_ANSI;
		c.W = W_ANSI;
		break;
	case AI_UNIKELLER: // norme unikeller
		c.N = N_Unikeller;
		c.A = A_Unikeller;
		c.k_102_factor_ai = k_102_factor_ai_Unikeller;
		c.VRef = Vref_Unikeller;
		c.W = W_Unikeller;
		break;
	case AI_NVH: // norme NVH
		c.N = N_NVH;
		c.A = A_NVH;
		c.k_102_factor_ai = k_102_factor_ai_NVH;
		c.VRef = Vref_NVH;
		c.W = W_NVH;
		break;
	default:
		assert(false);
	}
}

class CArticulationIndex
{
public:
	CArticulationIndex(ThirdOctaveParam param, AIType type);
	~CArticulationIndex();
	bool Init();
	int CalculateArticulationIndex(const int* buffer, uint32_t samples);
	const AIFrame* GetFrames(uint32_t* count);
	const float* Ai_time(uint32_t* count);
	double GetAI()
	{
		CalculateAI();
		return AI_DATA.ai;
	}
	int GetCalcResult(uint32_t index, short* frame);
	int GetCalcResultCount(uint32_t* count, uint32_t* frame_size);
	int GetMeanMaxResultCount(uint32_t* count_time, uint32_t* count_frame);
	int GetMeanMaxResult(bool bMean, double* data_time, double* data_freq);
	int ShortToDoubleFrame(const short* frame, double* frame_out);
	const double testForit();
	const double testForOther();
	void ResetResult(){
		m_result.clear();
	}
private:
	void GetFrameLevels(const double* frame);
	void ToctToAi();
	void CalculateAI();
private:
	ThirdOctaveParam m_param;
	CToctEx* m_pToctEx;
	AIType m_ai_type;
	CThirdOctave m_toct;
	Coef m_Coef;
	struct
	{
		double	ai; // lbl variable AI
		double	tab_ai[NB_BAND_AI]; // lbl variable AI
		int		ai_count; // lbl variable AI
	} AI_DATA;
	double m_tocts_max;
	double m_tocts_min;
	vector<AIFrame> m_result;
};
CArticulationIndex::CArticulationIndex(ThirdOctaveParam param, AIType type) : m_toct(param)
{
	m_param = param;
	m_ai_type = type;
	CoefType(type, m_Coef);
	memset(&AI_DATA, 0, sizeof(AI_DATA));
}

bool CArticulationIndex::Init()
{
	if (m_param.type == TOCT_TYPE_IEC_ALL)
		return false;
	m_tocts_max = SHRT_MIN;
	m_tocts_min = SHRT_MAX;
	m_pToctEx = m_toct.GetEx();
	m_pToctEx->InitEx([this](const double* frame){ GetFrameLevels(frame); });
	m_pToctEx->InitResultFunc(
		[this](){ return m_result.size(); },
		[this](size_t index)->float*{
		if (index >= m_result.size())
			return NULL;
		return m_result[index].ai; },
		[this](){return NB_BAND_AI; });
	return true;
}

CArticulationIndex::~CArticulationIndex()
{
	m_toct.ReleaseEx(m_pToctEx);
}

void CArticulationIndex::GetFrameLevels(const double * frame)
{
	double	tmp;
	int	c;
	int d = m_pToctEx->GetCalculateChannelCount() - DEB_BAND_AI - 1;
	m_result.push_back(AIFrame());
	//char d = TOCT.channels - 1;
	for (c = 0; c < NB_BAND_AI; c++)
	{
		tmp = m_pToctEx->GetLevel(frame[d--]);
		m_result.back().ai[c] = static_cast<float>(tmp);
	}
	ToctToAi();
}

int CArticulationIndex::CalculateArticulationIndex(const int* buffer, uint32_t samples)
{
	int ret = m_toct.Calculate(buffer, samples);
	if (samples == 0 && m_param.delay_flag)
		m_result.pop_back();
	AI_DATA.ai=(double)buffer[45];
	return ret;
}

void CArticulationIndex::ToctToAi()
{
	double w = 0, N = 0, A = 0, k_102_factor_ai = k_102_factor_ai_ANSI;
	int vRef = 0;

	N = m_Coef.N;
	A = m_Coef.A;
//	k_102_factor_ai = m_Coef.k_102_factor_ai;

	for (int i = 0; i < NB_BAND_AI; i++)
	{
		w = m_Coef.W[i];
		vRef = m_Coef.VRef[i];

		// formule AI = W*(Vref+A-V)/N
		double db = m_result.back().ai[i];	// valeur du 1/3 oct

		// lbl variable AI , somme de tous les ai
		AI_DATA.tab_ai[i] += pow(10, db / 10);

		// application de la formule
		double x1 = vRef + A - db;
		double val;
		// si Vref+A-V < 0 => AI = 0
		if (x1 < 0)
			val = 0;
		// si Vref+A-V > N => AI = W
		else if (x1 > N)
			val = w;
		else
			val = w* x1 / N;
		m_result.back().ai[i] = static_cast<float>(val);	// modifier k_102_factor_ai par un facteur plus appropri¨¦
	}
	AI_DATA.ai_count++; // lbl variable AI	
}

void CArticulationIndex::CalculateAI()
{
	double tab_ai[NB_BAND_AI];
	double w = 0, N = 0, A = 0;
	int vRef = 0;
	N = m_Coef.N;
	A = m_Coef.A;
	AI_DATA.ai = 0;
	if (AI_DATA.ai_count != 0)
	{
		for (int i = 0; i != NB_BAND_AI; i++)
		{
			tab_ai[i] = AI_DATA.tab_ai[i] / AI_DATA.ai_count;	// mean toct
			tab_ai[i] = 10 * log10(tab_ai[i]);	// mean toct en db

			// on passe du 1/3 octave ¨¤ l'AI
			w = m_Coef.W[i];
			vRef = m_Coef.VRef[i];

			// formule AI = W*(Vref+A-V)/N
			// application de la formule
			double x1 = vRef + A - tab_ai[i];
			double val;
			// si Vref+A-V < 0 => AI = 0
			if (x1 < 0)
				val = 0;
			// si Vref+A-V > N => AI = W
			else if (x1 > N)
				val = w;
			else
				val = w* x1 / N;

			AI_DATA.ai += val;
		}
	}
}

const AIFrame* CArticulationIndex::GetFrames(uint32_t* count)
{
	*count = static_cast<uint32_t>(m_result.size());
	return m_result.data();
}
const float* CArticulationIndex::Ai_time(uint32_t* frame_count){
	CMeanMax mean(NB_BAND_AI, true);
	const AIFrame* frames = GetFrames(frame_count);
	for (uint32_t i = 0; i != *frame_count; ++i)
	{
		mean.AddData(frames[i].ai);
	}
	const float * time_data = mean.GetTimeData(frame_count);
	return time_data;
}
const double CArticulationIndex::testForit(){
	return 12.3;
}
const double CArticulationIndex::testForOther(){
	return testForit();
}
//int CArticulationIndex::GetCalcResultCount(uint32_t* count, uint32_t* frame_size)
//{
//	return GetThirdOctaveCalcResultCount(m_third_octave_handle, count, frame_size);
//}
//
//int CArticulationIndex::GetCalcResult(uint32_t index, short* frame)
//{
//	return GetThirdOctaveCalcResult(m_third_octave_handle, index, frame);
//}
//
//int CArticulationIndex::GetMeanMaxResultCount(uint32_t* count_time, uint32_t* count_frame)
//{
//	auto& result_vec = GetAIResult(m_third_octave_handle);
//	*count_time = static_cast<uint32_t>(result_vec.size());
//	*count_frame = NB_BAND_AI;
//	return 0;
//}
//
//int CArticulationIndex::GetMeanMaxResult(bool bMean, double* data_time, double* data_freq)
//{
//	auto& result_vec = GetAIResult(m_third_octave_handle);
//	unique_ptr<double[]> mean_temp(new double[NB_BAND_AI]);
//	unique_ptr<double[]> mean_total(new double[NB_BAND_AI]());
//	int index = 0;
//	for (auto it = result_vec.begin(); it != result_vec.end(); ++it, ++index)
//	{
//		for (int i = 0; i != NB_BAND_AI; ++i)
//		{
//			mean_temp[i] = (*it)[i] * m_Coef.k_102_factor_ai;
//		}
//		if (data_time)
//		{
//			if (bMean)
//			{
//				data_time[index] = accumulate(mean_temp.get(), mean_temp.get() + NB_BAND_AI, 0.0);
//			}
//			else
//			{
//				data_time[index] = *(max_element(mean_temp.get(), mean_temp.get()+NB_BAND_AI,
//					[](double a, double b)
//				{
//					if (abs(a) < abs(b))
//						return true;
//					return false;
//				}));
//			}
//		}
//		if (bMean)
//		{
//			for (int i = 0; i != NB_BAND_AI; ++i)
//			{
//				mean_total[i] += mean_temp[i];
//			}
//		}
//		else
//		{
//			for (int i = 0; i != NB_BAND_AI; ++i)
//			{
//				if (abs(mean_total[i]) < abs(mean_temp[i]))
//					mean_total[i] = mean_temp[i];
//			}
//		}
//	}
//	if (bMean)
//	{
//		for (int i = 0; i != NB_BAND_AI; ++i)
//			mean_total[i] /= result_vec.size();
//	}
//	if (data_freq)
//	{
//		for (int i = 0; i != NB_BAND_AI; ++i)
//			data_freq[i] = mean_total[i];
//	}
//	return 0;
//}
//
//int CArticulationIndex::ShortToDoubleFrame(const short* frame, double* frame_out)
//{
//	for (int i = 0; i != NB_BAND_AI; ++i)
//		frame_out[i] = frame[i] * m_Coef.k_102_factor_ai;
//	return 0;
//}
//
//const AIFrame* CArticulationIndex::GetFrames(uint32_t* count)
//{
//	*count = static_cast<uint32_t>(m_result.size());
//	return &m_result[0];
//}
//
//HAI AI_API InitArticulationIndex(ThirdOctaveParam param, AIType type)
//{
//	CArticulationIndex* pAI = new CArticulationIndex(param, type);
//	if (pAI->Init())
//		return pAI;
//	else
//	{
//		delete pAI;
//		return NULL;
//	}
//}
//
//int AI_API CalculateArticulationIndex(HAI handle, int32_t* buffer, int samples)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->CalculateArticulationIndex(buffer, samples);
//}
//
//void AI_API UninitArticulationIndex(HAI handle)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	delete pAI;
//}
//
//int AI_API GetAICalcResult(HAI handle, uint32_t index, short* frame)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->GetCalcResult(index, frame);
//}
//
//int AI_API GetAICalcResultCount(HAI handle, uint32_t* count, uint32_t* frame_size)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->GetCalcResultCount(count, frame_size);
//}
//
//int AI_API GetAIMeanMaxResultCount(HAI handle, uint32_t* count_time, uint32_t* count_freq)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->GetMeanMaxResultCount(count_time, count_freq);
//}
//
//int AI_API GetAIMeanMaxResult(HAI handle, bool bMean, double* data_time, double* data_freq)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->GetMeanMaxResult(bMean, data_time, data_freq);
//}
//
//int AI_API AIShortToDoubleFrame(HAI handle, const short* frame, double* frame_out)
//{
//	CArticulationIndex* pAI = static_cast<CArticulationIndex*>(handle);
//	return pAI->ShortToDoubleFrame(frame, frame_out);
//}

CAICalc::CAICalc(ThirdOctaveParam param, AIType type) : m_pImpl(new CArticulationIndex(param, type))
{
	static_cast<CArticulationIndex*>(m_pImpl)->Init();
}

void CAICalc::Calculate(const int* data, uint32_t samples)
{
	static_cast<CArticulationIndex*>(m_pImpl)->CalculateArticulationIndex(data, samples);
}

const AIFrame* CAICalc::GetFrames(uint32_t* count)
{
	return static_cast<CArticulationIndex*>(m_pImpl)->GetFrames(count);
}

double CAICalc::GetAI()
{
	return static_cast<CArticulationIndex*>(m_pImpl)->GetAI();
}
const float* CAICalc::Ai_time(uint32_t* count){
	return  static_cast<CArticulationIndex*>(m_pImpl)->Ai_time(count);
}
void CAICalc::ResetResult(){
	return static_cast<CArticulationIndex*>(m_pImpl)->ResetResult();
}
CAICalc::~CAICalc()
{
	delete static_cast<CArticulationIndex*>(m_pImpl);
}

