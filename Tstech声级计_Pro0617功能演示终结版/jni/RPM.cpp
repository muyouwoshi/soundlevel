// RPM.cpp : Defines the exported functions for the DLL application.
//
#include "rpm.h"
#include <vector>
#include <algorithm>
using namespace std;

struct rps_struct {
	double time;
	double rounds_per_second;
	double spline_coeffs[3];
	double lin_coeff;
};

const double TIME_LABEL_POS = 0.5;
const size_t AVG_COUNT = 5;
const double DIFF_MAX = 5e-4;

const uint32_t RPM_COUNT = 400;
const uint32_t INDEX_RANGE = 3;
const float RPM_SCALE = 2.5f;
//! Round per Second (RPS) from Cubic Spline Coefficients at a desired time
double GetCubicSplineRPS(double time, rps_struct *rps) {
	time -= rps->time;
	return ((rps->spline_coeffs[2] * time + rps->spline_coeffs[1]) * time
			+ rps->spline_coeffs[0]) * time + rps->rounds_per_second;
}

//! Round per Second (RPS) by linear interpolation at a desired time
double GetLinearSplineRPS(double time, rps_struct *rps) {
	time -= rps->time;
	return rps->rounds_per_second + time * rps->lin_coeff;
}

class CRPMImpl {
public:
	CRPMImpl(double fs) :
			m_output_fs(fs) {
	}
	bool InitTacho(double fs, int pulse, float level) {
		m_tacho_fs = fs;
		m_first_data = true;
		m_rps_data.clear();
		m_current_sample = 0;
		m_pulses = pulse;
		m_level = level;
		m_prec = 1;
		m_pulses = 1;
		return true;
	}
	bool DecodeTacho(const float* data, uint32_t samples);
	const float* GetRPM(uint32_t* count);
private:
	bool IsTachoPulse(float data) {
		if (m_old_data == -1 && data != -1)
			return true;
		else
			return false;
	}
	double GetTachoPulseTime(float data) {
		return (m_level - data);
	}
	double GetRelDeltaRPM(int prec) {
		if (prec == 0)
			return 1;
		else if (prec == 1)
			return 0.005;
		return 0.001;
	}
	/*! (1/(T-dt)-1/T)*T = T/(T-dt)-1=s/(s-ds)-1 <= deltaRPM/trueRPM = r
	 *	- s:  true number of samples per pulse period (float number)
	 *	- ds: max. pulse period error in samples (<1, float number)
	 *	- r:  deltaRPM/trueRPM
	 * \return the minimum number of samples per pulse period so that the
	 * relative rotation speed error is smaller than r.
	 *
	 * The max. period error is assumed to be ds samples.
	 */
	uint32_t GetMinTachoSamples(double r, double ds) {
		if (r < 0.0001) // 0,1 pro mille
			r = 0.0001; //double samples = ceil( ds/r + ds*sqrt(1.0/r/r+1) );
		return (uint32_t)(ds * (1 + r) / r);
	}

	void RemoveBadRpsData() {
		double diff1;
		vector<double> diff;
		if (m_rps_data.empty())
			return;
		for (size_t k = 0; k != m_rps_data.size() - 1; ++k) {
			diff.push_back(m_rps_data[k + 1].time - m_rps_data[k].time);
		}
		size_t index = 1;
		while (index < m_rps_data.size() - 2) {
			if (abs(diff[index - 1] + diff[index + 1] - 2 * diff[index])
					< DIFF_MAX) {
				break;
			} else
				++index;
		}
		if (index == m_rps_data.size() - 2)
			index = 1;
		diff1 = abs(diff[index] - diff[index - 1]);
		vector < size_t > badIndex;
		for (size_t k = 2; k != diff.size(); ++k) {
			if (abs(diff[k] + diff[k - 1] - diff[k - 2] - diff1) < DIFF_MAX) {
				badIndex.push_back(k);
			}
		}
		for (auto it = badIndex.rbegin(); it != badIndex.rend(); ++it)
			m_rps_data.erase(m_rps_data.begin() + *it);
	}

	/*! transform from pulse periods to rotation speed within the error range
	 * \param rps
	 * \param numPulses : lenth of rps vector
	 * \param pulsesPerRev
	 * \param dtMin
	 * \return new length of rps vector
	 */
	/* for each non last element in rps vector (indices k)
	 *
	 *
	 */
	size_t PulseTimes2RPS(double dtMin) {
		double time;
		size_t n, k;
//		RemoveBadRpsData();
		if (m_rps_data.empty())
			return 0;
		for (k = 0; k != m_rps_data.size(); ++k) {
			time = m_rps_data[k].time + dtMin;
			n = k + 1;
			// find the first 
			while (n < m_rps_data.size() && m_rps_data[n].time <= time)
				n++;
			if (n == m_rps_data.size())
				break;
			time = m_rps_data[n].time - m_rps_data[k].time;
			m_rps_data[k].rounds_per_second = (double) (n - k) / time
					/ m_pulses;
//			m_rps_data[k].time += (TIME_LABEL_POS*time);
		}
		if (k > 0)
			k--;
		return k;
	}

	// Cubic Spline Coefficient computation:
	// John H.Matthews, Kurtis D.Fink: Numerical Methods Using Matlab.
	// Prentice Hall, 1999.

	/* for test purposes
	 RPS_VECTOR[0].time=0; RPS_VECTOR[0].rounds_per_second=0;
	 RPS_VECTOR[1].time=1; RPS_VECTOR[1].rounds_per_second=0.5;
	 RPS_VECTOR[2].time=2; RPS_VECTOR[2].rounds_per_second=2.0;
	 RPS_VECTOR[3].time=3; RPS_VECTOR[3].rounds_per_second=1.5;
	 getCubicSplineCoeffs( RPS_VECTOR, 4, 0 ); // natural
	 m0=m3=0, m1=2.4, m2=-3.6
	 P0=0.4*X^3+0.1*X, P1=-X^3+1.2*X^2+1.3*X+0.5, P2=0.6*X^3-1.8*X^2+0.7*X+2.0
	 getCubicSplineCoeffs( RPS_VECTOR, 4, 1, 0.2, -1 ); // clamped
	 m0=-0.36, m1=2.52, m2=-3.72, m3=0.36
	 P0=0.48*X^3-0.18*X^2+0.2*X, P1=-1.04*X^3+1.26*X^2+1.28*X+0.5, P2=0.68*X^3-1.86*X^2+0.68*X+2.0
	 getCubicSplineCoeffs( RPS_VECTOR, 4, 1, 0.2, -1, 1 ); // clamped, constrained
	 */
	void GetCubicSplineCoeffs(int use_slopes, double slope0, double slope1,
			int constrained_flag) {
		if (m_rps_data.size() < 2)
			return;
		// N := samples
		// Tri-diagonal linear system with N-2 equations									// Tri-Diagonales Gleichungssystem mit N-2 Gleichungen aufstellen
		// Compute the 2.derivatives mk of the polynomials Pk at the knots k=1,2,...,N-2	// Gesucht sind zunächst die 2.Ableitungen mk der Polynome Pk an den Knoten k=1,2,...,N-2
		// X		= x-xk		mit xk<=x<=xk+1
		// Pk(x) = yk + ak*X + bk*X^2 + ck*X^3
		// mk		= Pk''(xk)=2*bk, 2.Derivative
		// dk		= xk+1-xk
		// sk		= (yk+1-yk)/(xk+1-xk)
		// dk-1*mk-1 + 2*(dk-1+dk)*mk + dk*mk+1 = 6*(sk-sk-1) für k=1,...,N-2
		// = dk-1*mk-1 + 2*(xk+1-xk-1)*mk + dk*mk+1
		// Condition natural: m0=0 und mN-1=0
		// Condition clamped: Pk'(x0)=slope0 und Pk'(xN-1)=slope1
		// -> m0 = 3/d0*(s0-slope0)-m1/2=3/(x1-x0)*(s0-slope0)-m1/2
		//  mN-1	= 3/dN-2*(slope1-sN-2)-mN-2/2=3/(xN-1-xN-2)*(slope1-sN-2)-mN-2/2

		// N-1 Coefficients for linear Interpolation
		size_t k, n;
		size_t samples = m_rps_data.size();
		for (k = 1, n = 0; n != samples - 1; n++, k++)
			m_rps_data[n].lin_coeff = (m_rps_data[k].rounds_per_second
					- m_rps_data[n].rounds_per_second)
					/ (m_rps_data[k].time - m_rps_data[n].time);

		// min.3 samples for Cubic Splines
		if (m_rps_data.size() == 2 || constrained_flag != 0) {
			GetConstrainedCubicSplineCoeffs(use_slopes, slope0, slope1);//rps->spline_coeffs[0] = rps->lin_coeff;
			return;
		}

		// set the main diagonal with N-2 samples		// Hauptdiagonale mit N-2 Werten besetzen:
		// 2*(dk-1+dk)=2*(xk+1-xk-1) für k=1,..,N-2
		int main = 0, sub = 1, res = 2; //Indices for matrix handling	
		for (k = 2, n = 0; n < samples - 2; n++, k++)	//N-2 samples
			m_rps_data[n].spline_coeffs[main] = 2
					* (m_rps_data[k].time - m_rps_data[n].time);

		// clamped: m0=3/d0*(s0-slope0)-m1/2, mN-1=3/dN-2*(slope1-sN-2)-mN-2/2
		// modify first and last sample of the main diagonal
		if (use_slopes != 0) {
			m_rps_data[0].spline_coeffs[main] -= 0.5
					* (m_rps_data[1].time - m_rps_data[0].time);
			m_rps_data[samples - 3].spline_coeffs[main] -= 0.5
					* (m_rps_data[samples - 1].time
							- m_rps_data[samples - 2].time);
		}

		// Side diagonal with N-3 samples (dk for k=1,.,N-3) and (dk-1 for k=2,.,N-2) are identical
		for (k = 1, n = 0; n < samples - 3; n++, k++)
			m_rps_data[n].spline_coeffs[sub] = m_rps_data[k + 1].time
					- m_rps_data[k].time;

		// Result array with N-2 samples: 6*(sk-sk-1) for k=1,..,N-2
		for (k = 1, n = 0; n < samples - 2; n++, k++)
			m_rps_data[n].spline_coeffs[res] = 6
					* (m_rps_data[k].lin_coeff - m_rps_data[n].lin_coeff);

		// clamped: m0=3/d0*(s0-slope0)-m1/2, mN-1=3/dN-2*(slope1-sN-2)-mN-2/2
		// modify first and last sample of the results array
		if (use_slopes != 0) {
			m_rps_data[0].spline_coeffs[res] -= 3
					* (m_rps_data[0].lin_coeff - slope0);
			m_rps_data[samples - 3].spline_coeffs[res] -= 3
					* (slope1 - m_rps_data[samples - 2].lin_coeff);
		}

		// solve Tri-Diagonal system for k=1,...,N-2
		SolveTriDiag(samples - 2);
		// results are given in rps[k].spline_coeffs[main], k=1,2,...,samples-2

		// clamped: m0=3/d0*(s0-slope0)-m1/2, mN-1=3/dN-2*(slope1-sN-2)-mN-2/2
		// set the first and last value of the 2.derivatives (m0 und mN-1)	// ersten und letzten Wert der 2.Ableitungen setzen (m0 und mN-1)
		if (use_slopes != 0) {
			m_rps_data[0].spline_coeffs[main] = 3
					* (m_rps_data[0].lin_coeff - slope0)
					/ (m_rps_data[1].time - m_rps_data[0].time)
					- 0.5 * m_rps_data[1].spline_coeffs[main];
			m_rps_data[samples - 1].spline_coeffs[main] = 3
					* (slope1 - m_rps_data[samples - 2].lin_coeff)
					/ (m_rps_data[samples - 1].time
							- m_rps_data[samples - 2].time)
					- 0.5 * m_rps_data[samples - 2].spline_coeffs[main];
		} else	// natural
		{
			m_rps_data[0].spline_coeffs[main] = 0;
			m_rps_data[samples - 1].spline_coeffs[main] = 0;
		}

		// compute N-1 Cubic Spline Coefficients (k=0,1,...,N-2)
		// Pk(x) = yk + ak*X + bk*X^2 + ck*X^3, X=x-xk
		// ak		= sk-dk*(2*mk+mk+1)/6
		// bk		= mk/2;
		// ck		= (mk+1-mk)/6/dk
		double dk;
		for (n = 1, k = 0; k < samples - 1; k++, n++) {
			dk = m_rps_data[n].time - m_rps_data[k].time;
			m_rps_data[k].spline_coeffs[2] = (m_rps_data[n].spline_coeffs[main]
					- m_rps_data[k].spline_coeffs[main]) / 6.0 / dk;
			m_rps_data[k].spline_coeffs[1] = m_rps_data[k].spline_coeffs[main]
					/ 2.0;
			m_rps_data[k].spline_coeffs[0] = m_rps_data[k].lin_coeff
					- dk
							* (2 * m_rps_data[k].spline_coeffs[main]
									+ m_rps_data[n].spline_coeffs[main]) / 6.0;
		}
	}

	/*! Constrained Cubic Splines (without overshoot)
	 * CJC Kruger: Constrained Cubic Spline Interpolation (for Chemical Engineering)
	 * The first derivatives are preset in a way that the resulting		 * Die ersten Ableitungen der Polynome werden so vorbelegt, dass die resultierende
	 * spline curve can not overshoot.												 * Spline Kurve nicht schwingen kann.
	 * X:=x-xk
	 * Pk(x)   = yk+ak*X+bk*X^2+ck*X^3 = yk für x=xk, xk<=x<=xk+1
	 * Pk'(x)  = ak+2*bk*X+3*ck*X^2 = ak für x=xk
	 * Pk''(x) = 2*bk+6*ck*X = 2*bk für x=xk
	 * Pk-1(xk)=Pk(xk) und P'k-1(xk)=P'k(xk) aber P''k-1(xk)~=P''k(xk)
	 * sk:=(yk+1-yk)/(xk+1-xk)
	 * dk:=xk+1-xk
	 * -> bk=(3*sk-2*ak-ak+1)/dk
	 * -> ck=(ak+ak+1-2*sk)/dk/dk
	 * N:=samples
	 * Natural: P''0(x0)=0 und P''N-2(xN-1)=0
	 */
	void GetConstrainedCubicSplineCoeffs(int use_slopes, double slope0,
			double slope1) {
		// preset all 1.derivaties as average derivatives for all samples but the end samples n=1,2,..,N-2 // 1.Ableitungen als Mittelwerte für die Zwischenpunkte vorbelegen, an mit n=1,2,..,N-2

		size_t k, n;
		size_t samples = m_rps_data.size();
		for (k = 0, n = 1; n < samples - 1; n++, k++) {
			if ((m_rps_data[k].lin_coeff > 0 && m_rps_data[n].lin_coeff > 0)
					|| (m_rps_data[k].lin_coeff < 0
							&& m_rps_data[n].lin_coeff < 0))
				m_rps_data[n].spline_coeffs[0] = 2.0
						/ (1.0 / m_rps_data[k].lin_coeff
								+ 1.0 / m_rps_data[n].lin_coeff);//0.5*(rps[k].lin_coeff+rps[n].lin_coeff);
			else
				m_rps_data[n].spline_coeffs[0] = 0;
		}

		// define 1.derivatives a0 and aN-1 at the end points n=0 and n=N-1
		if (use_slopes != 0)		//clamped
				{
			m_rps_data[0].spline_coeffs[0] = slope0;
			m_rps_data[samples - 1].spline_coeffs[0] = slope1;
		} else if (samples == 2) // natural
				{
			m_rps_data[0].spline_coeffs[0] = m_rps_data[0].lin_coeff;
			m_rps_data[1].spline_coeffs[0] = m_rps_data[0].lin_coeff;
		} else {
			m_rps_data[0].spline_coeffs[0] = 1.5 * m_rps_data[0].lin_coeff
					- 0.5 * m_rps_data[1].spline_coeffs[0];
			m_rps_data[samples - 1].spline_coeffs[0] = 1.5
					* m_rps_data[samples - 2].lin_coeff
					- 0.5 * m_rps_data[samples - 2].spline_coeffs[0];
		}

		// Compute directly missing coefficients bn and cn for all Polynomials n=0,1,...,N-2
		double dk;
		for (k = 1, n = 0; n < samples - 1; n++, k++) {
			dk = m_rps_data[k].time - m_rps_data[n].time;
			// bn=(3*sn-2*an-an+1)/dn
			m_rps_data[n].spline_coeffs[1] = (3 * m_rps_data[n].lin_coeff
					- 2 * m_rps_data[n].spline_coeffs[0]
					- m_rps_data[k].spline_coeffs[0]) / dk;
			// cn=(an+an+1-2*sn)/dn/dn
			m_rps_data[n].spline_coeffs[2] = (m_rps_data[n].spline_coeffs[0]
					+ m_rps_data[k].spline_coeffs[0]
					- 2 * m_rps_data[n].lin_coeff) / dk / dk;
		}
	}

	/*% NUMERICAL METHODS: MATLAB Programs
	 %(c) 1999 by John H. Mathews and Kurtis D. Fink
	 %To accompany the textbook:
	 %NUMERICAL METHODS Using MATLAB,
	 %by John H. Mathews and Kurtis D. Fink
	 %ISBN 0-13-270042-5, (c) 1999
	 %PRENTICE HALL, INC.
	 %Upper Saddle River, NJ 07458*/
	// Example: 5*5 Matrix (len=5)
	//	H1	N1	0	0	0			M1			E1
	//	N1	H2	N2	0	0			M2			E2
	//	0	N2	H3	N3	0		*	M3		=	E3
	//	0	0	N3	H4	N4			M4			E4
	//	0	0	0	N4	H5			M5			E5
	// rps[k].spline_coeffs[0], k=0,1,...,len-1 main diagonal H1 to Hlen
	// rps[k].spline_coeffs[1], k=0,1,...,len-2 side diagonal N1 to Nlen-1
	// rps[k].spline_coeffs[2], k=0,1,...,len-1 results array E1 to Elen
	// results:
	// rps[k].spline_coeffs[0], k=1,2,...,len	solution array M1 bis Mlen
	// rps must have len+1 Elements!
	void SolveTriDiag(size_t len) {/*%	    - A is the sub diagonal of the coefficient matrix
	 %      - D is the main diagonal of the coefficient matrix
	 %      - C is the super diagonal of the coefficient matrix
	 %      - B is the constant vector of the linear system
	 %Output - X is the solution vector*/
		//N=length(B);
		double mult;
		int main = 0, sub = 1, res = 2;
		size_t n, k;
		for (n = 0, k = 1; k < len; k++, n++)						//for k=2:N
				{
			mult = m_rps_data[n].spline_coeffs[sub]
					/ m_rps_data[n].spline_coeffs[main];//	mult=A(k-1)/D(k-1);
			m_rps_data[k].spline_coeffs[main] -= mult
					* m_rps_data[n].spline_coeffs[sub];	//	D(k)=D(k)-mult*C(k-1);
			m_rps_data[k].spline_coeffs[res] -= mult
					* m_rps_data[n].spline_coeffs[res];	//	B(k)=B(k)-mult*B(k-1);
		}		//end
				//X(N)=B(N)/D(N);
		m_rps_data[k].spline_coeffs[main] = m_rps_data[n].spline_coeffs[res]
				/ m_rps_data[n].spline_coeffs[main];		//n=len-1, k=len
		for (k--, n--; k > 0; n--, k--)	//for n=N-1:-1:1
				{	//	X(n)=(B(n)-C(n)*X(n+1))/D(n);
			m_rps_data[k].spline_coeffs[main] =
					(m_rps_data[n].spline_coeffs[res]
							- m_rps_data[n].spline_coeffs[sub]
									* m_rps_data[k + 1].spline_coeffs[main])
							/ m_rps_data[n].spline_coeffs[main];
		}	// end
	}

	//! Derivatives at the Knots
	double GetDerivative(rps_struct rps, int deriv) {
		if (deriv == 0)
			return rps.rounds_per_second;
		else if (deriv == 1)
			return rps.spline_coeffs[0];
		else if (deriv == 2)
			return 2 * rps.spline_coeffs[1];
		else if (deriv == 3)
			return 6 * rps.spline_coeffs[2];
		else
			return 0;
	}

	/*! returns a RPM value for a given time using Cubic spline or
	 * linear interpolation
	 */
	double GetRPMvalue(double time_sec, bool use_cubic_splines,
			uint32_t& rps_index) {
		double rps;
		if (time_sec <= m_rps_data[0].time) {// RPM value left of the given time
			rps = m_rps_data[0].rounds_per_second;
			rps_index = 0;
		} else if (time_sec >= m_rps_data.back().time) {// RPM value right of the given time
			rps_index = static_cast<uint32_t>(m_rps_data.size() - 1);
			rps = m_rps_data[rps_index].rounds_per_second;
		} else {	// RPM value between the existing RPM samples
			rps_struct *p_rps;
			if (rps_index >= m_rps_data.size())
				p_rps = &m_rps_data.back();
			else
				p_rps = &m_rps_data[rps_index];
			while (p_rps->time <= time_sec)
				p_rps++;
			while (p_rps->time > time_sec)
				p_rps--;
			rps_index = static_cast<uint32_t>(p_rps - &m_rps_data[0]);

			if (use_cubic_splines == 1)
				rps = GetCubicSplineRPS(time_sec, p_rps);
			else
				rps = GetLinearSplineRPS(time_sec, p_rps);
		}

		return rps * 60; 	// rotation frequency to RPM
	}
	// Genererates a new RPM array with a given sample rate using the already
	// calculated Cubic Spline Coefficients.
	// rpm_rate: desired sample rate of RPM data
	bool GetNewRPMvector(bool use_cubic_splines) {
		if (m_rps_data.empty())
			return false;
		// allocate RPM data at the given rate
		vector<float> rpm_data;
		uint32_t rps_index = 0;
		uint32_t samples = (uint32_t) ceil(
				m_current_sample / m_tacho_fs * m_output_fs);
		for (uint32_t k = 0; k != samples; k++) {
			float data = static_cast<float>(GetRPMvalue(k / m_output_fs,
					use_cubic_splines, rps_index));
			if (data < 0)
				data = 0;
			rpm_data.push_back(data);
		}

		auto minmax = minmax_element(rpm_data.begin(), rpm_data.end());
		if (*minmax.first == *minmax.second)
			return false;
		m_rpm_data = std::move(rpm_data);
		return true;
	}
private:
	double m_output_fs;
	double m_tacho_fs;
	float m_old_data;
	bool m_first_data;
	uint32_t m_current_sample;
	vector<rps_struct> m_rps_data;
	vector<float> m_rpm_data;
	int m_prec;
	int m_pulses;
	float m_level;
};

bool CRPMImpl::DecodeTacho(const float* data, uint32_t samples) {
	if (m_first_data) {
		m_old_data = data[0];
		m_first_data = false;
	}
	for (uint32_t i = 0; i != samples; ++i, ++m_current_sample) {
		if (IsTachoPulse(data[i])) {
			m_rps_data.push_back(rps_struct());
			m_rps_data.back().time = ((m_current_sample - 1)
					+ GetTachoPulseTime(data[i])) / m_tacho_fs;
		}
		m_old_data = data[i];
	}
	return true;
}

const float* CRPMImpl::GetRPM(uint32_t* count) {
	// transform pulse period times into rotation speed
	// a max.error<1 sample is assumed due to the linear time interpolation between two
	// samples around the pulse slope for the estimation of the period
	double dtMin = GetMinTachoSamples(GetRelDeltaRPM(m_prec), 0.2) / m_tacho_fs;
	size_t rps_count = PulseTimes2RPS(dtMin);
	if (rps_count < 2) {
		*count = 0;
		return nullptr;
	}
	m_rps_data.resize(rps_count);
	// Constrained Cubic Spline Coefficients (no overshoot)
	GetCubicSplineCoeffs(1, 0, 0, 1);
	if (!GetNewRPMvector(1)) {
		*count = 0;
		return nullptr;
	}
	*count = static_cast<uint32_t>(m_rpm_data.size());
	return m_rpm_data.data();
}

CRPM::CRPM(double out_fs) :m_pImpl(new CRPMImpl(out_fs))
{
}
CRPM::~CRPM() {
	delete static_cast<CRPMImpl*>(m_pImpl);
}

bool CRPM::InitTacho(double fs, int pulse,
		float level) {
	return static_cast<CRPMImpl*>(m_pImpl)->InitTacho(fs, pulse, level);
}

bool CRPM::DecodeTacho(const float* data, uint32_t samples) {
	return static_cast<CRPMImpl*>(m_pImpl)->DecodeTacho(data, samples);
}

const float* CRPM::GetRPM(uint32_t* count) {
	return static_cast<CRPMImpl*>(m_pImpl)->GetRPM(count);
}
bool RPM_API CRPMCalc::Init(double fs, const float* rpm_data, uint32_t count,
		uint32_t data_count) {
	m_rpm_rate = fs;
	struct RPM_INDEX {
		uint32_t index;
		float rpm;
	};
	uint32_t index = 0;
	vector<RPM_INDEX> rpm_index;
	for (uint32_t i = 0; i != count; ++i) {
		rpm_index.push_back( { index++, rpm_data[i] });
	}
	sort(rpm_index.begin(), rpm_index.end(), [](RPM_INDEX a, RPM_INDEX b)
	{
		if (a.rpm < b.rpm)
		return true;
		else
		return false;
	});
	float min_rpm = rpm_index.front().rpm;
	float max_rpm = rpm_index.back().rpm;
	uint32_t diff = static_cast<uint32_t>(max_rpm - min_rpm);
	if (data_count == 0 || data_count >= RPM_COUNT) {
		static uint32_t diff_rpm[] = { 25, 20, 15, 10, 5, 4, 3, 2, 1 };
		for (index = 0; index != sizeof(diff_rpm) / sizeof(diff_rpm[0]); ++index) {
			if (diff / diff_rpm[index] >= RPM_COUNT)
				break;
		}
		if (index == sizeof(diff_rpm) / sizeof(diff_rpm[0]))
			index =  sizeof(diff_rpm) / sizeof(diff_rpm[0])- 1;
		m_delta_rpm = diff_rpm[index];
	} else {
		m_delta_rpm = int(diff / data_count);
	}
	m_start_rpm = static_cast<uint32_t>(min_rpm) / m_delta_rpm * m_delta_rpm;
	m_end_rpm = static_cast<uint32_t>(max_rpm) / m_delta_rpm * m_delta_rpm;
	uint32_t rpm_count = (m_end_rpm - m_start_rpm) / m_delta_rpm + 1;
	index = 0;
	for (uint32_t i = 0; i != rpm_count; ++i) {
		uint32_t current_rpm = m_start_rpm + i * m_delta_rpm;
		float min_diff = abs(current_rpm - rpm_index[index].rpm);
		float old_diff = min_diff;
		for (size_t j = index + 1; j != rpm_index.size(); ++j) {
			float current_diff = abs(current_rpm - rpm_index[j].rpm);
			if (current_diff < min_diff) {
				min_diff = current_diff;
				index = static_cast<uint32_t>(j);
			} else {
				if (current_diff > old_diff)
					break;
			}
			old_diff = current_diff;
		}
		m_rpm_index.push_back(vector<uint32_t>());
		if (min_diff < RPM_SCALE * m_delta_rpm) {
			uint32_t start_index, end_index;
			if (index < INDEX_RANGE) {
				start_index = 0;
			} else {
				start_index = index - INDEX_RANGE;
			}
			if (index + INDEX_RANGE > rpm_index.size()) {
				end_index = static_cast<uint32_t>(rpm_index.size());
			} else {
				end_index = index + INDEX_RANGE;
			}
			if (end_index - start_index == INDEX_RANGE) {
				if (start_index == 0)
					end_index = start_index + 2 * INDEX_RANGE;
				else
					start_index = end_index - 2 * INDEX_RANGE;
			}
			for (int k = start_index; k != end_index; ++k) {
				if (abs(rpm_index[k].rpm - current_rpm)
						< RPM_SCALE * m_delta_rpm) {
					m_rpm_index.back().push_back(rpm_index[k].index);
				}
			}
		}
	}
	for (auto& vec : m_rpm_index) {
		if (vec.empty()) {
			m_rpm_index.clear();
			return false;
		}
	}
	return true;
}
