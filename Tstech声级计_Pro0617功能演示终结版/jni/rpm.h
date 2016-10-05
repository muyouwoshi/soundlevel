#pragma once
#ifdef RPM_EXPORTS
#define RPM_API
#else
#define RPM_API
#endif
#include <cstdint>
#include <vector>
#include <memory>

class RPM_API CRPM {
public:
	CRPM(double out_fs);
	~CRPM();
	bool InitTacho(double fs, int pulse = 1, float level = 0.0f);
	bool DecodeTacho(const float* data, uint32_t samples);
	const float* GetRPM(uint32_t* count);
private:
	CRPM(const CRPM&);
	CRPM& operator =(const CRPM&);
private:
	void* const m_pImpl;
};

class CRPMCalc {
public:
	CRPMCalc() {

	}
	bool  Init(double fs, const float* rpm_data, uint32_t count,
			uint32_t data_count=0);
	bool HaveRPMData() {
		return !m_rpm_index.empty();
	}
	bool GetRPMRange(uint32_t& startRPM, uint32_t& endRPM, uint32_t& deltaRPM) {
		if (m_rpm_index.empty())
			return false;
		startRPM = m_start_rpm;
		endRPM = m_end_rpm;
		deltaRPM = m_delta_rpm;
		return true;
	}

	template<typename It, typename T = typename It::value_type::element_type>
	std::vector<std::unique_ptr<T[]>> GetRPMData(It first, It last,
			uint32_t size, double time_interval) {
		std::vector<std::unique_ptr<T[]>> ret;
		double dt = 1 / m_rpm_rate / time_interval;
		for (auto& vec : m_rpm_index) {
			std::unique_ptr<T[]> total(new T[size]());
			for (auto index : vec) {
				size_t data_index = static_cast<size_t>(index * dt);
				if ((intptr_t) data_index >= last - first)
					data_index = static_cast<size_t>(last - first - 1);
				for (uint32_t i = 0; i != size; ++i)
					total[i] += (*(first + data_index))[i];
			}
			for (uint32_t i = 0; i != size; ++i)
				total[i] /= vec.size();
			ret.push_back(std::move(total));
		}
		return ret;
	}

	template<typename It>
	std::vector<typename It::value_type> GetRPMData(It first, It last,
			int time_interval) {
		typedef typename It::value_type T;
		std::vector<T> ret;
		float dt = static_cast<float>(1000.0f / m_rpm_rate / time_interval);
		for (auto& vec : m_rpm_index) {
			T total = 0;
			for (auto index : vec) {
				size_t data_index = static_cast<size_t>(index * dt);
				if (first + data_index >= last)
					data_index = static_cast<size_t>(last - first - 1);
				total += *(first + data_index);
			}
			ret.push_back(total / vec.size());
		}
		return ret;
	}
private:
	CRPMCalc(const CRPMCalc&);
	CRPMCalc& operator =(const CRPMCalc&);
private:
	std::vector<std::vector<uint32_t>> m_rpm_index;
	double m_rpm_rate;
	uint32_t m_start_rpm;
	uint32_t m_end_rpm;
	uint32_t m_delta_rpm;
};
