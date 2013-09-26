package com.franktom.horizon;

public class SolarPosition {

    /*============================================================================
    *
    *     Define the function codes
    *
    *----------------------------------------------------------------------------*/
    static final int L_DOY    =0x0001;
    static final int  L_GEOM  = 0x0002;
    static final int  L_ZENETR= 0x0004;
    static final int  L_SSHA  = 0x0008;
    static final int  L_SBCF  = 0x0010;
    static final int  L_TST   = 0x0020;
    static final int  L_SRSS  = 0x0040;
    static final int L_SOLAZM =0x0080;
    static final int L_REFRAC =0x0100;
    static final int L_AMASS  =0x0200;
    static final int L_PRIME  =0x0400;
    static final int L_TILT  = 0x0800;
    static final int L_ETR    =0x1000;
    static final int L_ALL    =0xFFFF;

    /*============================================================================
    *
    *     Define the bit-wise masks for each function
    *
    *----------------------------------------------------------------------------*/
    static final int S_DOY    = ( L_DOY                          );
    static final int S_GEOM   = ( L_GEOM   | S_DOY               );
    static final int S_ZENETR = ( L_ZENETR | S_GEOM              );
    static final int S_SSHA   = ( L_SSHA   | S_GEOM              );
    static final int S_SBCF   = ( L_SBCF   | S_SSHA              );
    static final int S_TST    = ( L_TST    | S_GEOM              );
    static final int S_SRSS   = ( L_SRSS   | S_SSHA   | S_TST    );
    static final int S_SOLAZM = ( L_SOLAZM | S_ZENETR            );
    static final int S_REFRAC = ( L_REFRAC | S_ZENETR            );
    static final int S_AMASS  = ( L_AMASS  | S_REFRAC            );
    static final int S_PRIME  = ( L_PRIME  | S_AMASS             );
    static final int S_TILT   = ( L_TILT   | S_SOLAZM | S_REFRAC );
    static final int S_ETR    = ( L_ETR    | S_REFRAC            );
    static final int S_ALL    = ( L_ALL                          );


    /*============================================================================
    *
    *     Enumerate the error codes
    *     (Bit positions are from least significant to most significant)
    *
    *----------------------------------------------------------------------------*/
    /*          Code          Bit       Parameter            Range
          ===============     ===  ===================  =============   */
    static final int S_YEAR_ERROR  =0;/*  0   year                  1950 -  2050   */
    static final int S_MONTH_ERROR =1;/*  1   month                    1 -    12   */
    static final int S_DAY_ERROR   =2;/*  2   day-of-month             1 -    31   */
    static final int S_DOY_ERROR   =3;/*  3   day-of-year              1 -   366   */
    static final int S_HOUR_ERROR  =4;/*  4   hour                     0 -    24   */
    static final int S_MINUTE_ERROR=5;/*  5   minute                   0 -    59   */
    static final int S_SECOND_ERROR=6;/*  6   second                   0 -    59   */
    static final int S_TZONE_ERROR =7;/*  7   time zone              -12 -    12   */
    static final int S_INTRVL_ERROR=8;/*  8   interval (seconds)       0 - 28800   */
    static final int S_LAT_ERROR   =9;/*  9   latitude               -90 -    90   */
    static final int S_LON_ERROR   =10;/* 10   longitude             -180 -   180   */
    static final int S_TEMP_ERROR  =11;/* 11   temperature (deg. C)  -100 -   100   */
    static final int S_PRESS_ERROR =12;/* 12   pressure (millibars)     0 -  2000   */
    static final int S_TILT_ERROR  =13;/* 13   tilt                   -90 -    90   */
    static final int S_ASPECT_ERROR=14;/* 14   aspect                -360 -   360   */
    static final int S_SBWID_ERROR =15;/* 15   shadow band width (cm)   1 -   100   */
    static final int S_SBRAD_ERROR =16;/* 16   shadow band radius (cm)  1 -   100   */
    static final int S_SBSKY_ERROR =17;/* 17   shadow band sky factor  -1 -     1   */

    static class posdata
    {
      /***** ALPHABETICAL LIST OF COMMON VARIABLES *****/
                               /* Each comment begins with a 1-column letter code:
                                  I:  INPUT variable
                                  O:  OUTPUT variable
                                  T:  TRANSITIONAL variable used in the algorithm,
                                      of interest only to the solar radiation
                                      modelers, and available to you because you
                                      may be one of them.

                                  The FUNCTION column indicates which sub-function
                                  within solpos must be switched on using the
                                  "function" parameter to calculate the desired
                                  output variable.  All function codes are
                                  defined in the solpos.h file.  The default
                                  S_ALL switch calculates all output variables.
                                  Multiple functions may be or'd to create a
                                  composite function switch.  For example,
                                  (S_TST | S_SBCF). Specifying only the functions
                                  for required output variables may allow solpos
                                  to execute more quickly.

                                  The S_DOY mask works as a toggle between the
                                  input date represented as a day number (daynum)
                                  or as month and day.  To set the switch (to
                                  use daynum input), the function is or'd; to
                                  clear the switch (to use month and day input),
                                  the function is inverted and and'd.

                                  For example:
                                      pdat.function |= S_DOY (sets daynum input)
                                      pdat.function &= ~S_DOY (sets month and day input)

                                  Whichever date form is used, S_solpos will
                                  calculate and return the variables(s) of the
                                  other form.  See the soltest.c program for
                                  other examples. */

      /* VARIABLE        I/O  Function    Description */
      /* -------------  ----  ----------  ---------------------------------------*/

      int   day;       /* I/O: S_DOY      Day of month (May 27 = 27, etc.)
                                            solpos will CALCULATE this by default,
                                            or will optionally require it as input
                                            depending on the setting of the S_DOY
                                            function switch. */
      int   daynum;    /* I/O: S_DOY      Day number (day of year; Feb 1 = 32 )
                                            solpos REQUIRES this by default, but
                                            will optionally calculate it from
                                            month and day depending on the setting
                                            of the S_DOY function switch. */
      int   function;  /* I:              Switch to choose functions for desired
                                            output. */
      int   hour;      /* I:              Hour of day, 0 - 23, DEFAULT = 12 */
      int   interval;  /* I:              Interval of a measurement period in
                                            seconds.  Forces solpos to use the
                                            time and date from the interval
                                            midpoint. The INPUT time (hour,
                                            minute, and second) is assumed to
                                            be the END of the measurement
                                            interval. */
      int   minute;    /* I:              Minute of hour, 0 - 59, DEFAULT = 0 */
      int   month;     /* I/O: S_DOY      Month number (Jan = 1, Feb = 2, etc.)
                                            solpos will CALCULATE this by default,
                                            or will optionally require it as input
                                            depending on the setting of the S_DOY
                                            function switch. */
      int   second;    /* I:              Second of minute, 0 - 59, DEFAULT = 0 */
      int   year;      /* I:              4-digit year (2-digit year is NOT
                                           allowed */

      /***** FLOATS *****/

      float amass;      /* O:  S_AMASS    Relative optical airmass */
      float ampress;    /* O:  S_AMASS    Pressure-corrected airmass */
      float aspect;     /* I:             Azimuth of panel surface (direction it
                                            faces) N=0, E=90, S=180, W=270,
                                            DEFAULT = 180 */
      float azim;       /* O:  S_SOLAZM   Solar azimuth angle:  N=0, E=90, S=180,
                                            W=270 */
      float cosinc;     /* O:  S_TILT     Cosine of solar incidence angle on
                                            panel */
      float coszen;     /* O:  S_REFRAC   Cosine of refraction corrected solar
                                            zenith angle */
      float dayang;     /* T:  S_GEOM     Day angle (daynum*360/year-length)
                                            degrees */
      float declin;     /* T:  S_GEOM     Declination--zenith angle of solar noon
                                            at equator, degrees NORTH */
      float eclong;     /* T:  S_GEOM     Ecliptic longitude, degrees */
      float ecobli;     /* T:  S_GEOM     Obliquity of ecliptic */
      float ectime;     /* T:  S_GEOM     Time of ecliptic calculations */
      float elevetr;    /* O:  S_ZENETR   Solar elevation, no atmospheric
                                            correction (= ETR) */
      float elevref;    /* O:  S_REFRAC   Solar elevation angle,
                                            deg. from horizon, refracted */
      float eqntim;     /* T:  S_TST      Equation of time (TST - LMT), minutes */
      float erv;        /* T:  S_GEOM     Earth radius vector
                                            (multiplied to solar constant) */
      float etr;        /* O:  S_ETR      Extraterrestrial (top-of-atmosphere)
                                            W/sq m global horizontal solar
                                            irradiance */
      float etrn;       /* O:  S_ETR      Extraterrestrial (top-of-atmosphere)
                                            W/sq m direct normal solar
                                            irradiance */
      float etrtilt;    /* O:  S_TILT     Extraterrestrial (top-of-atmosphere)
                                            W/sq m global irradiance on a tilted
                                            surface */
      float gmst;       /* T:  S_GEOM     Greenwich mean sidereal time, hours */
      float hrang;      /* T:  S_GEOM     Hour angle--hour of sun from solar noon,
                                            degrees WEST */
      float julday;     /* T:  S_GEOM     Julian Day of 1 JAN 2000 minus
                                            2,400,000 days (in order to regain
                                            single precision) */
      float latitude;   /* I:             Latitude, degrees north (south negative) */
      float longitude;  /* I:             Longitude, degrees east (west negative) */
      float lmst;       /* T:  S_GEOM     Local mean sidereal time, degrees */
      float mnanom;     /* T:  S_GEOM     Mean anomaly, degrees */
      float mnlong;     /* T:  S_GEOM     Mean longitude, degrees */
      float rascen;     /* T:  S_GEOM     Right ascension, degrees */
      float press;      /* I:             Surface pressure, millibars, used for
                                            refraction correction and ampress */
      float prime;      /* O:  S_PRIME    Factor that normalizes Kt, Kn, etc. */
      float sbcf;       /* O:  S_SBCF     Shadow-band correction factor */
      float sbwid;      /* I:             Shadow-band width (cm) */
      float sbrad;      /* I:             Shadow-band radius (cm) */
      float sbsky;      /* I:             Shadow-band sky factor */
      float solcon;     /* I:             Solar constant (NREL uses 1367 W/sq m) */
      float ssha;       /* T:  S_SRHA     Sunset(/rise) hour angle, degrees */
      float sretr;      /* O:  S_SRSS     Sunrise time, minutes from midnight,
                                            local, WITHOUT refraction */
      float ssetr;      /* O:  S_SRSS     Sunset time, minutes from midnight,
                                            local, WITHOUT refraction */
      float temp;       /* I:             Ambient dry-bulb temperature, degrees C,
                                            used for refraction correction */
      float tilt;       /* I:             Degrees tilt from horizontal of panel */
      float timezone;   /* I:             Time zone, east (west negative).
                                          USA:  Mountain = -7, Central = -6, etc. */
      float tst;        /* T:  S_TST      True solar time, minutes from midnight */
      float tstfix;     /* T:  S_TST      True solar time - local standard time */
      float unprime;    /* O:  S_PRIME    Factor that denormalizes Kt', Kn', etc. */
      float utime;      /* T:  S_GEOM     Universal (Greenwich) standard time */
      float zenetr;     /* T:  S_ZENETR   Solar zenith angle, no atmospheric
                                            correction (= ETR) */
      float zenref;     /* O:  S_REFRAC   Solar zenith angle, deg. from zenith,
                                            refracted */
    };

    static class trigdata /* used to pass calculated values locally */
    {
        float cd;       /* cosine of the declination */
        float ch;       /* cosine of the hour angle */
        float cl;       /* cosine of the latitude */
        float sd;       /* sine of the declination */
        float sl;       /* sine of the latitude */
        public trigdata() {}
    };


    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    *
    * Temporary global variables used only in this file:
    *
    *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
      static int  month_days[][] = { { 0,   0,  31,  59,  90, 120, 151,
                                           181, 212, 243, 273, 304, 334 },
                                        { 0,   0,  31,  60,  91, 121, 152,
                                           182, 213, 244, 274, 305, 335 } };
                       /* cumulative number of days prior to beginning of month */

      static float degrad = 57.295779513f; /* converts from radians to degrees */
      static float raddeg = 0.0174532925f; /* converts from degrees to radians */

    /*============================================================================
    *    Local function prototypes
    ============================================================================*/
  /*  static long validate ( posdata pdat);
    static void dom2doy( posdata pdat );
    static void doy2dom( posdata pdat );
    static void geometry ( posdata pdat );
    static void zen_no_ref ( posdata pdat, trigdata tdat );
    static void ssha(  posdata pdat,  trigdata tdat );
    static void sbcf(  posdata pdat,  trigdata tdat );
    static void tst(  posdata pdat );
    static void srss(  posdata pdat );
    static void sazm(  posdata pdat,  trigdata tdat );
    static void refrac(  posdata pdat );
    static void amass(  posdata pdat );
    static void prime(  posdata pdat );
    static void etr(  posdata pdat );
    static void tilt(  posdata pdat );
    static void localtrig(  posdata pdat,  trigdata tdat );*/

    /*============================================================================
    *    Long integer function S_solpos, adapted from the VAX solar libraries
    *
    *    This function calculates the apparent solar position and the
    *    intensity of the sun (theoretical maximum solar energy) from
    *    time and place on Earth.
    *
    *    Requires (from the struct posdata parameter):
    *        Date and time:
    *            year
    *            daynum   (requirement depends on the S_DOY switch)
    *            month    (requirement depends on the S_DOY switch)
    *            day      (requirement depends on the S_DOY switch)
    *            hour
    *            minute
    *            second
    *            interval  DEFAULT 0
    *        Location:
    *            latitude
    *            longitude
    *        Location/time adjuster:
    *            timezone
    *        Atmospheric pressure and temperature:
    *            press     DEFAULT 1013.0 mb
    *            temp      DEFAULT 10.0 degrees C
    *        Tilt of flat surface that receives solar energy:
    *            aspect    DEFAULT 180 (South)
    *            tilt      DEFAULT 0 (Horizontal)
    *        Function Switch (codes defined in solpos.h)
    *            function  DEFAULT S_ALL
    *
    *    Returns (via the struct posdata parameter):
    *        everything defined in the struct posdata in solpos.h.
    *----------------------------------------------------------------------------*/

    static long S_solpos (posdata pdat) {
      long retval;

      //trigdata trigdat = new trigdata();
      trigdata tdat = new trigdata();

      /* initialize the trig structure */
      tdat.sd = -999.0f; /* flag to force calculation of trig data */
      tdat.cd =    1.0f;
      tdat.ch =    1.0f; /* set the rest of these to something safe */
      tdat.cl =    1.0f;
      tdat.sl =    1.0f;

      if ((retval = validate ( pdat )) != 0) /* validate the inputs */
        return retval;


      if ( (pdat.function & L_DOY)>0 )
        doy2dom( pdat );                /* convert input doy to month-day */
      else
        dom2doy( pdat );                /* convert input month-day to doy */

      if ( (pdat.function & L_GEOM)>0 )
        geometry( pdat );               /* do basic geometry calculations */

      if ( (pdat.function & L_ZENETR)>0 )  /* etr at non-refracted zenith angle */
        zen_no_ref( pdat, tdat );

      if ( (pdat.function & L_SSHA)>0 )    /* Sunset hour calculation */
        ssha( pdat, tdat );

      if ( (pdat.function & L_SBCF)>0 )    /* Shadowband correction factor */
        sbcf( pdat, tdat );

      if ( (pdat.function & L_TST)>0 )     /* true solar time */
        tst( pdat );

      if ( (pdat.function & L_SRSS)>0 )    /* sunrise/sunset calculations */
        srss( pdat );

      if ( (pdat.function & L_SOLAZM)>0 )  /* solar azimuth calculations */
        sazm( pdat, tdat );

      if ( (pdat.function & L_REFRAC)>0 )  /* atmospheric refraction calculations */
        refrac( pdat );

      if ( (pdat.function & L_AMASS)>0 )   /* airmass calculations */
        amass( pdat );

      if ( (pdat.function & L_PRIME)>0 )   /* kt-prime/unprime calculations */
        prime( pdat );

      if ( (pdat.function & L_ETR)>0 )     /* ETR and ETRN (refracted) */
        etr( pdat );

      if ( (pdat.function & L_TILT)>0 )    /* tilt calculations */
        tilt( pdat );

        return 0;
    }


    /*============================================================================
    *    Void function S_init
    *
    *    This function initiates all of the input parameters in the struct
    *    posdata passed to S_solpos().  Initialization is either to nominal
    *    values or to out of range values, which forces the calling program to
    *    specify parameters.
    *
    *    NOTE: This function is optional if you initialize ALL input parameters
    *          in your calling code.  Note that the required parameters of date
    *          and location are deliberately initialized out of bounds to force
    *          the user to enter real-world values.
    *
    *    Requires: Pointer to a posdata structure, members of which are
    *           initialized.
    *
    *    Returns: Void
    *----------------------------------------------------------------------------*/
    static void S_init(posdata pdat)
    {
      pdat.day       =    -99;   /* Day of month (May 27 = 27, etc.) */
      pdat.daynum    =   -999;   /* Day number (day of year; Feb 1 = 32 ) */
      pdat.hour      =    -99;   /* Hour of day, 0 - 23 */
      pdat.minute    =    -99;   /* Minute of hour, 0 - 59 */
      pdat.month     =    -99;   /* Month number (Jan = 1, Feb = 2, etc.) */
      pdat.second    =    -99;   /* Second of minute, 0 - 59 */
      pdat.year      =    -99;   /* 4-digit year */
      pdat.interval  =      0;   /* instantaneous measurement interval */
      pdat.aspect    =  180.0f;   /* Azimuth of panel surface (direction it
                                        faces) N=0, E=90, S=180, W=270 */
      pdat.latitude  =  -99.0f;   /* Latitude, degrees north (south negative) */
      pdat.longitude = -999.0f;   /* Longitude, degrees east (west negative) */
      pdat.press     = 1013.0f;   /* Surface pressure, millibars */
      pdat.solcon    = 1367.0f;   /* Solar constant, 1367 W/sq m */
      pdat.temp      =   15.0f;   /* Ambient dry-bulb temperature, degrees C */
      pdat.tilt      =    0.0f;   /* Degrees tilt from horizontal of panel */
      pdat.timezone  =  -99.0f;   /* Time zone, east (west negative). */
      pdat.sbwid     =    7.6f;   /* Eppley shadow band width */
      pdat.sbrad     =   31.7f;   /* Eppley shadow band radius */
      pdat.sbsky     =   0.04f;   /* Drummond factor for partly cloudy skies */
      pdat.function  =  S_ALL;   /* compute all parameters */
    }


    /*============================================================================
    *    Local long int function validate
    *
    *    Validates the input parameters
    *----------------------------------------------------------------------------*/
    static long validate (posdata pdat)
    {

      long retval = 0;  /* start with no errors */

      /* No absurd dates, please. */
      if ( (pdat.function & L_GEOM)>0 )
      {
        if ( (pdat.year < 1950) || (pdat.year > 2050) ) /* limits of algoritm */
          retval |= (1L << S_YEAR_ERROR);
        if ( (pdat.function & S_DOY)==0 && ((pdat.month < 1) || (pdat.month > 12)))
          retval |= (1L << S_MONTH_ERROR);
        if ( (pdat.function & S_DOY)==0 && ((pdat.day < 1) || (pdat.day > 31)) )
          retval |= (1L << S_DAY_ERROR);
        if ( (pdat.function & S_DOY)>0 && ((pdat.daynum < 1) || (pdat.daynum > 366)) )
          retval |= (1L << S_DOY_ERROR);

        /* No absurd times, please. */
        if ( (pdat.hour < 0) || (pdat.hour > 24) )
          retval |= (1L << S_HOUR_ERROR);
        if ( (pdat.minute < 0) || (pdat.minute > 59) )
          retval |= (1L << S_MINUTE_ERROR);
        if ( (pdat.second < 0) || (pdat.second > 59) )
          retval |= (1L << S_SECOND_ERROR);
        if ( (pdat.hour == 24) && (pdat.minute > 0) ) /* no more than 24 hrs */
          retval |= ( (1L << S_HOUR_ERROR) | (1L << S_MINUTE_ERROR) );
        if ( (pdat.hour == 24) && (pdat.second > 0) ) /* no more than 24 hrs */
          retval |= ( (1L << S_HOUR_ERROR) | (1L << S_SECOND_ERROR) );
        if ( Math.abs (pdat.timezone) > 12.0 )
          retval |= (1L << S_TZONE_ERROR);
        if ( (pdat.interval < 0) || (pdat.interval > 28800) )
          retval |= (1L << S_INTRVL_ERROR);

        /* No absurd locations, please. */
        if ( Math.abs (pdat.longitude) > 180.0 )
          retval |= (1L << S_LON_ERROR);
        if ( Math.abs (pdat.latitude) > 90.0 )
          retval |= (1L << S_LAT_ERROR);
      }

      /* No silly temperatures or pressures, please. */
      if ( (pdat.function & L_REFRAC)>0 && (Math.abs(pdat.temp) > 100.0) )
        retval |= (1L << S_TEMP_ERROR);
      if ( (pdat.function & L_REFRAC)>0 &&
        (pdat.press < 0.0) || (pdat.press > 2000.0) )
        retval |= (1L << S_PRESS_ERROR);

      /* No out of bounds tilts, please */
      if ( (pdat.function & L_TILT)>0 && (Math.abs (pdat.tilt) > 180.0) )
        retval |= (1L << S_TILT_ERROR);
      if ( (pdat.function & L_TILT)>0 && (Math.abs (pdat.aspect) > 360.0) )
        retval |= (1L << S_ASPECT_ERROR);

      /* No oddball shadowbands, please */
      if ( (pdat.function & L_SBCF)>0 &&
           (pdat.sbwid < 1.0) || (pdat.sbwid > 100.0) )
        retval |= (1L << S_SBWID_ERROR);
      if ( (pdat.function & L_SBCF)>0 &&
           (pdat.sbrad < 1.0) || (pdat.sbrad > 100.0) )
        retval |= (1L << S_SBRAD_ERROR);
      if ( (pdat.function & L_SBCF)>0 && ( Math.abs (pdat.sbsky) > 1.0) )
        retval |= (1L << S_SBSKY_ERROR);

      return retval;
    }


    /*============================================================================
    *    Local Void function dom2doy
    *
    *    Converts day-of-month to day-of-year
    *
    *    Requires (from struct posdata parameter):
    *            year
    *            month
    *            day
    *
    *    Returns (via the struct posdata parameter):
    *            year
    *            daynum
    *----------------------------------------------------------------------------*/
    static void dom2doy( posdata pdat )
    {
      pdat.daynum = pdat.day + month_days[0][pdat.month];

      /* (adjust for leap year) */
      if ( ((pdat.year % 4) == 0) &&
             ( ((pdat.year % 100) != 0) || ((pdat.year % 400) == 0) ) &&
             (pdat.month > 2) )
          pdat.daynum += 1;
    }


    /*============================================================================
    *    Local void function doy2dom
    *
    *    This function computes the month/day from the day number.
    *
    *    Requires (from struct posdata parameter):
    *        Year and day number:
    *            year
    *            daynum
    *
    *    Returns (via the struct posdata parameter):
    *            year
    *            month
    *            day
    *----------------------------------------------------------------------------*/
    static void doy2dom(posdata pdat)
    {
      int  imon;  /* Month (month_days) array counter */
      int  leap;  /* leap year switch */

        /* Set the leap year switch */
        if ( ((pdat.year % 4) == 0) &&
             ( ((pdat.year % 100) != 0) || ((pdat.year % 400) == 0) ) )
            leap = 1;
        else
            leap = 0;

        /* Find the month */
        imon = 12;
        while ( pdat.daynum <= month_days [leap][imon] )
            --imon;

        /* Set the month and day of month */
        pdat.month = imon;
        pdat.day   = pdat.daynum - month_days[leap][imon];
    }


    /*============================================================================
    *    Local Void function geometry
    *
    *    Does the underlying geometry for a given time and location
    *----------------------------------------------------------------------------*/
    static void geometry ( posdata pdat )
    {
      float bottom;      /* denominator (bottom) of the fraction */
      float c2;          /* cosine of d2 */
      float cd;          /* cosine of the day angle or delination */
      float d2;          /* pdat.dayang times two */
      float delta;       /* difference between current year and 1949 */
      float s2;          /* sine of d2 */
      float sd;          /* sine of the day angle */
      float top;         /* numerator (top) of the fraction */
      int   leap;        /* leap year counter */

      /* Day angle */
          /*  Iqbal, M.  1983.  An Introduction to Solar Radiation.
                Academic Press, NY., page 3 */
         pdat.dayang = 360.0f * ( pdat.daynum - 1 ) / 365.0f;

        /* Earth radius vector * solar constant = solar energy */
            /*  Spencer, J. W.  1971.  Fourier series representation of the
                position of the sun.  Search 2 (5), page 172 */
        sd     = (float)Math.sin (raddeg * pdat.dayang);
        cd     = (float)Math.cos (raddeg * pdat.dayang);
        d2     = 2.0f * pdat.dayang;
        c2     = (float)Math.cos (raddeg * d2);
        s2     = (float)Math.sin (raddeg * d2);

        pdat.erv  = 1.000110f + 0.034221f * cd + 0.001280f * sd;
        pdat.erv  += 0.000719f * c2 + 0.000077f * s2;

        /* Universal Coordinated (Greenwich standard) time */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.utime =
            pdat.hour * 3600.0f +
            pdat.minute * 60.0f +
            pdat.second -
            (float)pdat.interval / 2.0f;
        pdat.utime = pdat.utime / 3600.0f - pdat.timezone;

        /* Julian Day minus 2,400,000 days (to eliminate roundoff errors) */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */

        /* No adjustment for century non-leap years since this function is
           bounded by 1950 - 2050 */
        delta    = pdat.year - 1949;
        leap     = (int) ( delta / 4.0 );
        pdat.julday =
            32916.5f + delta * 365.0f + leap + pdat.daynum + pdat.utime / 24.0f;

        /* Time used in the calculation of ecliptic coordinates */
        /* Noon 1 JAN 2000 = 2,400,000 + 51,545 days Julian Date */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.ectime = pdat.julday - 51545.0f;

        /* Mean longitude */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.mnlong  = 280.460f + 0.9856474f * pdat.ectime;

        /* (dump the multiples of 360, so the answer is between 0 and 360) */
        pdat.mnlong -= 360.0 * (int) ( pdat.mnlong / 360.0 );
        if ( pdat.mnlong < 0.0 )
            pdat.mnlong += 360.0;

        /* Mean anomaly */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.mnanom  = 357.528f + 0.9856003f * pdat.ectime;

        /* (dump the multiples of 360, so the answer is between 0 and 360) */
        pdat.mnanom -= 360.0 * (int) ( pdat.mnanom / 360.0 );
        if ( pdat.mnanom < 0.0 )
            pdat.mnanom += 360.0;

        /* Ecliptic longitude */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.eclong  = (float)(pdat.mnlong + 1.915 * Math.sin ( pdat.mnanom * raddeg ) +
                        0.020 * Math.sin ( 2.0 * pdat.mnanom * raddeg ));

        /* (dump the multiples of 360, so the answer is between 0 and 360) */
        pdat.eclong -= 360.0 * (int) ( pdat.eclong / 360.0 );
        if ( pdat.eclong < 0.0 )
            pdat.eclong += 360.0;

        /* Obliquity of the ecliptic */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */

        /* 02 Feb 2001 SMW corrected sign in the following line */
    /*  pdat.ecobli = 23.439 + 4.0e-07 * pdat.ectime;     */
        pdat.ecobli = 23.439f - 4.0e-07f * pdat.ectime;

        /* Declination */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.declin = (float)(degrad * Math.asin ( Math.sin (pdat.ecobli * raddeg) *
                                   Math.sin (pdat.eclong * raddeg) ));

        /* Right ascension */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        top      =  (float)Math.cos ( raddeg * pdat.ecobli ) * (float)Math.sin ( raddeg * pdat.eclong );
        bottom   =  (float)Math.cos ( raddeg * pdat.eclong );

        pdat.rascen =  degrad * (float)Math.atan2 ( top, bottom );

        /* (make it a positive angle) */
        if ( pdat.rascen < 0.0 )
            pdat.rascen += 360.0;

        /* Greenwich mean sidereal time */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.gmst  = 6.697375f + 0.0657098242f * pdat.ectime + pdat.utime;

        /* (dump the multiples of 24, so the answer is between 0 and 24) */
        pdat.gmst -= 24.0 * (int) ( pdat.gmst / 24.0 );
        if ( pdat.gmst < 0.0 )
            pdat.gmst += 24.0;

        /* Local mean sidereal time */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.lmst  = pdat.gmst * 15.0f + pdat.longitude;

        /* (dump the multiples of 360, so the answer is between 0 and 360) */
        pdat.lmst -= 360.0 * (int) ( pdat.lmst / 360.0 );
        if ( pdat.lmst < 0.)
            pdat.lmst += 360.0;

        /* Hour angle */
            /*  Michalsky, J.  1988.  The Astronomical Almanac's algorithm for
                approximate solar position (1950-2050).  Solar Energy 40 (3),
                pp. 227-235. */
        pdat.hrang = pdat.lmst - pdat.rascen;

        /* (force it between -180 and 180 degrees) */
        if ( pdat.hrang < -180.0 )
            pdat.hrang += 360.0;
        else if ( pdat.hrang > 180.0 )
            pdat.hrang -= 360.0;
    }


    /*============================================================================
    *    Local Void function zen_no_ref
    *
    *    ETR solar zenith angle
    *       Iqbal, M.  1983.  An Introduction to Solar Radiation.
    *            Academic Press, NY., page 15
    *----------------------------------------------------------------------------*/
    static void zen_no_ref ( posdata pdat, trigdata tdat )
    {
      float cz;          /* cosine of the solar zenith angle */

        localtrig( pdat, tdat );
        cz = tdat.sd * tdat.sl + tdat.cd * tdat.cl * tdat.ch;

        /* (watch out for the roundoff errors) */
        if ( Math.abs (cz) > 1.0 ) {
            if ( cz >= 0.0 )
                cz =  1.0f;
            else
                cz = -1.0f;
        }

        pdat.zenetr   = (float)Math.acos ( cz ) * degrad;

        /* (limit the degrees below the horizon to 9 [+90 . 99]) */
        if ( pdat.zenetr > 99.0f )
            pdat.zenetr = 99.0f;

        pdat.elevetr = 90.0f - pdat.zenetr;
    }


    /*============================================================================
    *    Local Void function ssha
    *
    *    Sunset hour angle, degrees
    *       Iqbal, M.  1983.  An Introduction to Solar Radiation.
    *            Academic Press, NY., page 16
    *----------------------------------------------------------------------------*/
    static void ssha( posdata pdat, trigdata tdat )
    {
      float cssha;       /* cosine of the sunset hour angle */
      float cdcl;        /* ( cd * cl ) */

        localtrig( pdat, tdat );
        cdcl    = tdat.cd * tdat.cl;

        if ( Math.abs ( cdcl ) >= 0.001 ) {
            cssha = -tdat.sl * tdat.sd / cdcl;

            /* This keeps the cosine from blowing on roundoff */
            if ( cssha < -1.0  )
                pdat.ssha = 180.0f;
            else if ( cssha > 1.0 )
                pdat.ssha = 0.0f;
            else
                pdat.ssha = degrad * (float)Math.acos ( cssha );
        }
        else if ( ((pdat.declin >= 0.0) && (pdat.latitude > 0.0 )) ||
                  ((pdat.declin <  0.0) && (pdat.latitude < 0.0 )) )
            pdat.ssha = 180.0f;
        else
            pdat.ssha = 0.0f;
    }


    /*============================================================================
    *    Local Void function sbcf
    *
    *    Shadowband correction factor
    *       Drummond, A. J.  1956.  A contribution to absolute pyrheliometry.
    *            Q. J. R. Meteorol. Soc. 82, pp. 481-493
    *----------------------------------------------------------------------------*/
    static void sbcf( posdata pdat, trigdata tdat )
    {
      float p, t1, t2;   /* used to compute sbcf */

        localtrig( pdat, tdat );
        p       = 0.6366198f * pdat.sbwid / pdat.sbrad * (float)Math.pow (tdat.cd,3);
        t1      = tdat.sl * tdat.sd * pdat.ssha * raddeg;
        t2      = tdat.cl * tdat.cd * (float)Math.sin ( pdat.ssha * raddeg );
        pdat.sbcf = pdat.sbsky + 1.0f / ( 1.0f - p * ( t1 + t2 ) );

    }


    /*============================================================================
    *    Local Void function tst
    *
    *    TST . True Solar Time = local standard time + TSTfix, time
    *      in minutes from midnight.
    *        Iqbal, M.  1983.  An Introduction to Solar Radiation.
    *            Academic Press, NY., page 13
    *----------------------------------------------------------------------------*/
    static void tst( posdata pdat )
    {
        pdat.tst    = ( 180.0f + pdat.hrang ) * 4.0f;
        pdat.tstfix =
            pdat.tst -
            (float)pdat.hour * 60.0f -
            pdat.minute -
            (float)pdat.second / 60.0f +
            (float)pdat.interval / 120.0f; /* add back half of the interval */

        /* bound tstfix to this day */
        while ( pdat.tstfix >  720.0 )
            pdat.tstfix -= 1440.0;
        while ( pdat.tstfix < -720.0 )
            pdat.tstfix += 1440.0;

        pdat.eqntim =
            pdat.tstfix + 60.0f * pdat.timezone - 4.0f * pdat.longitude;

    }


    /*============================================================================
    *    Local Void function srss
    *
    *    Sunrise and sunset times (minutes from midnight)
    *----------------------------------------------------------------------------*/
    static void srss( posdata pdat )
    {
        if ( pdat.ssha <= 1.0 ) {
            pdat.sretr   =  2999.0f;
            pdat.ssetr   = -2999.0f;
        }
        else if ( pdat.ssha >= 179.0 ) {
            pdat.sretr   = -2999.0f;
            pdat.ssetr   =  2999.0f;
        }
        else {
            pdat.sretr   = 720.0f - 4.0f * pdat.ssha - pdat.tstfix;
            pdat.ssetr   = 720.0f + 4.0f * pdat.ssha - pdat.tstfix;
        }
    }


    /*============================================================================
    *    Local Void function sazm
    *
    *    Solar azimuth angle
    *       Iqbal, M.  1983.  An Introduction to Solar Radiation.
    *            Academic Press, NY., page 15
    *----------------------------------------------------------------------------*/
    static void sazm( posdata pdat, trigdata tdat )
    {
      float ca;          /* cosine of the solar azimuth angle */
      float ce;          /* cosine of the solar elevation */
      float cecl;        /* ( ce * cl ) */
      float se;          /* sine of the solar elevation */

        localtrig( pdat, tdat );
        ce         = (float)Math.cos ( raddeg * pdat.elevetr );
        se         = (float)Math.sin ( raddeg * pdat.elevetr );

        pdat.azim     = 180.0f;
        cecl       = ce * tdat.cl;
        if ( Math.abs ( cecl ) >= 0.001 ) {
            ca     = ( se * tdat.sl - tdat.sd ) / cecl;
            if ( ca > 1.0 )
                ca = 1.0f;
            else if ( ca < -1.0 )
                ca = -1.0f;

            pdat.azim = 180.0f - (float)Math.acos ( ca ) * degrad;
            if ( pdat.hrang > 0 )
                pdat.azim  = 360.0f - pdat.azim;
        }
    }


    /*============================================================================
    *    Local Int function refrac
    *
    *    Refraction correction, degrees
    *        Zimmerman, John C.  1981.  Sun-pointing programs and their
    *            accuracy.
    *            SAND81-0761, Experimental Systems Operation Division 4721,
    *            Sandia National Laboratories, Albuquerque, NM.
    *----------------------------------------------------------------------------*/
    static void refrac( posdata pdat )
    {
      float prestemp;    /* temporary pressure/temperature correction */
      float refcor;      /* temporary refraction correction */
      float tanelev;     /* tangent of the solar elevation angle */

        /* If the sun is near zenith, the algorithm bombs; refraction near 0 */
        if ( pdat.elevetr > 85.0 )
            refcor = 0.0f;

        /* Otherwise, we have refraction */
        else {
            tanelev = (float)Math.tan ( raddeg * pdat.elevetr );
            if ( pdat.elevetr >= 5.0 )
                refcor  = 58.1f / tanelev -
                          0.07f / ( (float)Math.pow (tanelev,3) ) +
                          0.000086f / ( (float)Math.pow (tanelev,5) );
            else if ( pdat.elevetr >= -0.575 )
                refcor  = 1735.0f +
                          pdat.elevetr * ( -518.2f + pdat.elevetr * ( 103.4f +
                          pdat.elevetr * ( -12.79f + pdat.elevetr * 0.711f ) ) );
            else
                refcor  = -20.774f / tanelev;

            prestemp    =
                ( pdat.press * 283.0f ) / ( 1013.0f * ( 273.0f + pdat.temp ) );
            refcor     *= prestemp / 3600.0f;
        }

        /* Refracted solar elevation angle */
        pdat.elevref = pdat.elevetr + refcor;

        /* (limit the degrees below the horizon to 9) */
        if ( pdat.elevref < -9.0 )
            pdat.elevref = -9.0f;

        /* Refracted solar zenith angle */
        pdat.zenref  = 90.0f - pdat.elevref;
        pdat.coszen  = (float)Math.cos( raddeg * pdat.zenref );
    }


    /*============================================================================
    *    Local Void function  amass
    *
    *    Airmass
    *       Kasten, F. and Young, A.  1989.  Revised optical air mass
    *            tables and approximation formula.  Applied Optics 28 (22),
    *            pp. 4735-4738
    *----------------------------------------------------------------------------*/
    static void amass( posdata pdat )
    {
        if ( pdat.zenref > 93.0 )
        {
            pdat.amass   = -1.0f;
            pdat.ampress = -1.0f;
        }
        else
        {
            pdat.amass =
                1.0f / ( (float)Math.cos (raddeg * pdat.zenref) + 0.50572f *
                        (float)Math.pow ((96.07995f - pdat.zenref),-1.6364f) );

            pdat.ampress   = pdat.amass * pdat.press / 1013.0f;
        }
    }


    /*============================================================================
    *    Local Void function prime
    *
    *    Prime and Unprime
    *    Prime  converts Kt to normalized Kt', etc.
    *       Unprime deconverts Kt' to Kt, etc.
    *            Perez, R., P. Ineichen, Seals, R., & Zelenka, A.  1990.  Making
    *            full use of the clearness index for parameterizing hourly
    *            insolation conditions. Solar Energy 45 (2), pp. 111-114
    *----------------------------------------------------------------------------*/
    static void prime( posdata pdat )
    {
        pdat.unprime = 1.031f * (float)Math.exp ( -1.4f / ( 0.9f + 9.4f / pdat.amass ) ) + 0.1f;
        pdat.prime   = 1.0f / pdat.unprime;
    }


    /*============================================================================
    *    Local Void function etr
    *
    *    Extraterrestrial (top-of-atmosphere) solar irradiance
    *----------------------------------------------------------------------------*/
    static void etr( posdata pdat )
    {
        if ( pdat.coszen > 0.0 ) {
            pdat.etrn = pdat.solcon * pdat.erv;
            pdat.etr  = pdat.etrn * pdat.coszen;
        }
        else {
            pdat.etrn = 0.0f;
            pdat.etr  = 0.0f;
        }
    }


    /*============================================================================
    *    Local Void function localtrig
    *
    *    Does trig on internal variable used by several functions
    *----------------------------------------------------------------------------*/
    static final int SD_MASK = ( L_ZENETR | L_SSHA | S_SBCF | S_SOLAZM );
    static final int SL_MASK = ( L_ZENETR | L_SSHA | S_SBCF | S_SOLAZM );
    static final int CL_MASK = ( L_ZENETR | L_SSHA | S_SBCF | S_SOLAZM );
    static final int CD_MASK = ( L_ZENETR | L_SSHA | S_SBCF );
    static final int CH_MASK = ( L_ZENETR );
    static void localtrig( posdata pdat, trigdata tdat )
    {
    /* define masks to prevent calculation of uninitialized variables */

        if ( tdat.sd < -900.0 )  /* sd was initialized -999 as flag */
        {
          tdat.sd = 1.0f;  /* reflag as having completed calculations */
          if ( (pdat.function | CD_MASK)>0 )
            tdat.cd = (float)Math.cos ( raddeg * pdat.declin );
          if ( (pdat.function | CH_MASK)>0 )
            tdat.ch = (float)Math.cos ( raddeg * pdat.hrang );
          if ( (pdat.function | CL_MASK)>0 )
            tdat.cl = (float)Math.cos ( raddeg * pdat.latitude );
          if ( (pdat.function | SD_MASK)>0 )
            tdat.sd = (float)Math.sin ( raddeg * pdat.declin );
          if ( (pdat.function | SL_MASK)>0 )
            tdat.sl = (float)Math.sin ( raddeg * pdat.latitude );
        }
    }


    /*============================================================================
    *    Local Void function tilt
    *
    *    ETR on a tilted surface
    *----------------------------------------------------------------------------*/
    static void tilt( posdata pdat )
    {
      float ca;          /* cosine of the solar azimuth angle */
      float cp;          /* cosine of the panel aspect */
      float ct;          /* cosine of the panel tilt */
      float sa;          /* sine of the solar azimuth angle */
      float sp;          /* sine of the panel aspect */
      float st;          /* sine of the panel tilt */
      float sz;          /* sine of the refraction corrected solar zenith angle */


        /* Cosine of the angle between the sun and a tipped flat surface,
           useful for calculating solar energy on tilted surfaces */
        ca      = (float)Math.cos ( raddeg * pdat.azim );
        cp      = (float)Math.cos ( raddeg * pdat.aspect );
        ct      = (float)Math.cos ( raddeg * pdat.tilt );
        sa      = (float)Math.sin ( raddeg * pdat.azim );
        sp      = (float)Math.sin ( raddeg * pdat.aspect );
        st      = (float)Math.sin ( raddeg * pdat.tilt );
        sz      = (float)Math.sin ( raddeg * pdat.zenref );
        pdat.cosinc  = pdat.coszen * ct + sz * st * ( ca * cp + sa * sp );

        if ( pdat.cosinc > 0.0 )
            pdat.etrtilt = pdat.etrn * pdat.cosinc;
        else
            pdat.etrtilt = 0.0f;

    }


    /*============================================================================
    *    Void function S_decode
    *
    *    This function decodes the error codes from S_solpos return value
    *
    *    Requires the long integer return value from S_solpos
    *
    *    Returns descriptive text to stderr
    *----------------------------------------------------------------------------*/
    static void S_decode(long code, posdata pdat)
    {
/*      if ( (code & (1L << S_YEAR_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the year: %d [1950-2050]\n",
          pdat.year);
      if ( (code & (1L << S_MONTH_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the month: %d\n",
          pdat.month);
      if ( (code & (1L << S_DAY_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the day-of-month: %d\n",
          pdat.day);
      if ( (code & (1L << S_DOY_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the day-of-year: %d\n",
          pdat.daynum);
      if ( (code & (1L << S_HOUR_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the hour: %d\n",
          pdat.hour);
      if ( (code & (1L << S_MINUTE_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the minute: %d\n",
          pdat.minute);
      if ( (code & (1L << S_SECOND_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the second: %d\n",
          pdat.second);
      if ( (code & (1L << S_TZONE_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the time zone: %f\n",
          pdat.timezone);
      if ( (code & (1L << S_INTRVL_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the interval: %d\n",
          pdat.interval);
      if ( (code & (1L << S_LAT_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the latitude: %f\n",
          pdat.latitude);
      if ( (code & (1L << S_LON_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the longitude: %f\n",
          pdat.longitude);
      if ( (code & (1L << S_TEMP_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the temperature: %f\n",
          pdat.temp);
      if ( (code & (1L << S_PRESS_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the pressure: %f\n",
          pdat.press);
      if ( (code & (1L << S_TILT_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the tilt: %f\n",
          pdat.tilt);
      if ( (code & (1L << S_ASPECT_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the aspect: %f\n",
          pdat.aspect);
      if ( (code & (1L << S_SBWID_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the shadowband width: %f\n",
          pdat.sbwid);
      if ( (code & (1L << S_SBRAD_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the shadowband radius: %f\n",
          pdat.sbrad);
      if ( (code & (1L << S_SBSKY_ERROR))>0 )
        fprintf(stderr, "S_decode ==> Please fix the shadowband sky factor: %f\n",
          pdat.sbsky);*/
    }

}
