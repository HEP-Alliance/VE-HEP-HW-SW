// https://docs.rust-embedded.org/book/peripherals/a-first-attempt.html

use volatile_register::*;
use core::fmt;

#[repr(C)]
pub struct Uart {
	data: RW<u32>,
	status: RO<u32>,
	clock_divider: RO<u32>,
	frame_config: RO<u32>,
}

impl Uart {
	/** There can only be one */
	const THE_UART: *mut Uart = 0xF0010000 as *mut _;

	pub fn new() -> &'static mut Self {
		// SAFETY: trololol
		unsafe { &mut *Self::THE_UART as &mut Uart }
	}

	#[inline(always)]
	fn available(&self) -> bool {
		(self.status.read() >> 16) & 0xFF != 0
	}

	#[inline]
	pub fn write(&self, word: u8) {
		while !self.available() {
		}
		// SAFETY: The side effect caused by this is known and intended
		unsafe { self.data.write(word as u32); }
	}

	pub fn print(&self, text: &[u8]) {
		for word in text {
			self.write(*word);
// 				if *word == 10 {
// 					self.write(b'\n');
// 				} else {
// 					let HEX_CHARS_LOWER: &[u8; 16] = b"0123456789abcdef";
// 					self.write(HEX_CHARS_LOWER[(*word >> 4) as usize]);
// 					self.write(HEX_CHARS_LOWER[(*word & 0xF) as usize]);
// 					self.write(b' ');
// 				}
		}
	}
}

impl fmt::Write for Uart {
	fn write_str(&mut self, s: &str) -> fmt::Result {
		self.print(s.as_bytes());
		Ok(())
	}
}
