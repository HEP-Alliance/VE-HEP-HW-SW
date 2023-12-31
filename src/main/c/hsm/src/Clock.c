/* Microsoft Reference Implementation for TPM 2.0
 *
 *  The copyright in this software is being made available under the BSD License,
 *  included below. This software may be subject to other third party and
 *  contributor rights, including patent rights, and no such rights are granted
 *  under this license.
 *
 *  Copyright (c) Microsoft Corporation
 *
 *  All rights reserved.
 *
 *  BSD License
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list
 *  of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *  list of conditions and the following disclaimer in the documentation and/or other
 *  materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ""AS IS""
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//** Introduction
// This file contains the routines that are used by the simulator to mimic
// a hardware clock on a TPM.

// In this implementation, all the time values are measured in millisecond.
// However, the precision of the clock functions may be implementation dependent.

//** Includes and Data Definitions
//#include "PlatformData.h"
//#include "Platform_fp.h"
//#include "TpmFail_fp.h"
#include <assert.h>
#include <stdint.h>


//** Simulator Functions
//*** Introduction
// This set of functions is intended to be called by the simulator environment in
// order to simulate hardware events.

int _plat__RealTime() {
  return 0;
}

//***_plat__TimerReset()
// This function sets current system clock time as t0 for counting TPM time.
// This function is called at a power on event to reset the clock. When the clock
// is reset, the indication that the clock was stopped is also set.
void
_plat__TimerReset(
    void
    )
{
    return;
}

//*** _plat__TimerRestart()
// This function should be called in order to simulate the restart of the timer
// should it be stopped while power is still applied.
void
_plat__TimerRestart(
    void
    )
{
    return;
}


//** Functions Used by TPM
//*** Introduction
// These functions are called by the TPM code. They should be replaced by
// appropriated hardware functions.

//***_plat__TimerRead()
// This function provides access to the tick timer of the platform. The TPM code 
// uses this value to drive the TPM Clock.
//
// The tick timer is supposed to run when power is applied to the device. This timer
// should not be reset by time events including _TPM_Init. It should only be reset
// when TPM power is re-applied.
//
// If the TPM is run in a protected environment, that environment may provide the
// tick time to the TPM as long as the time provided by the environment is not
// allowed to go backwards. If the time provided by the system can go backwards
// during a power discontinuity, then the _plat__Signal_PowerOn should call
// _plat__TimerReset().
//
// The code in this function should be replaced by a read of a hardware tick timer.
LIB_EXPORT uint64_t
_plat__TimerRead(
    void
    )
{
    return 0;
}



//*** _plat__TimerWasReset()
// This function is used to interrogate the flag indicating if the tick timer has 
// been reset.
//
// If the resetFlag parameter is SET, then the flag will be CLEAR before the 
// function returns.
int
_plat__TimerWasReset(
   void          
    )
{
    return 0;
}

//*** _plat__TimerWasStopped()
// This function is used to interrogate the flag indicating if the tick timer has 
// been stopped. If so, this is typically a reason to roll the nonce.
//
// This function will CLEAR the s_timerStopped flag before returning. This provides
// functionality that is similar to status register that is cleared when read. This
// is the model used here because it is the one that has the most impact on the TPM
// code as the flag can only be accessed by one entity in the TPM. Any other
// implementation of the hardware can be made to look like a read-once register.
int
_plat__TimerWasStopped(
    void
    )
{
    return 0;
}

//***_plat__ClockAdjustRate()
// Adjust the clock rate
void
_plat__ClockAdjustRate(
    int adjust
    )
{
    (void) adjust;
}

