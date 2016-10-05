#define _USE_MATH_DEFINES
#include <math.h>
#include "math_ex.h"
#include <stdint.h>
using namespace std;

double round(double data, unsigned char stellen)
{
	if(stellen == 0)
		return round(data);
	double int_part;
	double float_part = modf(data, &int_part);
	double factor = pow(10.0, stellen);
	float_part *= factor;
	return int_part + round(float_part) / factor;
}

/*! computes the frequency response of a digital filter by evaluation of the transfer function
* on one point of the unit circle.
* \param fw:		pointer to forward coefficients similar to Matlab definition
* \param fb:		pointer to backward coefficients similar to Matlab definition
* \param f_fs_2:	evaluation frequency normalized to the nyquist frequency (>=0 and <=1)
* \param orderfw:	number of forward coefficients-1
* \param orderfb: number of feedback coefficients-1
* \return
*/
complex<double> z2f(double *fw, double *fb, double f_fs_2, unsigned long orderfw, unsigned long orderfb)
{
	double arg = -f_fs_2*M_PI, phi;
	complex<double> ztmp;
	unsigned long k;

	complex<double> znum(fw[orderfw], 0);
	complex<double> zden(fb[orderfb], 0);

	for (k = 0; k<orderfw; k++)
	{
		phi = arg * (orderfw - k);
		ztmp = complex<double>(fw[k] * cos(phi), fw[k] * sin(phi));
		znum += ztmp;
	}

	for (k = 0; k<orderfb; k++)
	{
		phi = arg * (orderfb - k);
		ztmp = complex<double>(fb[k] * cos(phi), fb[k] * sin(phi));
		zden += ztmp;
	}
//	return cmpdiv(znum, zden);
	return znum / zden;
}
