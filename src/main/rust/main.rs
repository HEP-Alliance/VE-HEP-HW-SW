#![feature(asm)]
#![feature(global_asm)]
#![no_main]
#![no_std]
#![allow(dead_code)]

use core::panic::PanicInfo;
use core::fmt::Write;

use sha2::Digest;

mod uart;

mod gpio {
	use volatile_register::*;
	
	#[repr(C)]
	pub struct Gpio {
		pub input: RO<u32>,
		pub output: WO<u32>,
		pub output_enable: WO<u32>,
	}
	
	impl Gpio {
		pub unsafe fn gpio_a() -> &'static Self {
			&*(0xF0000000 as *mut Gpio) as &Gpio
		}

		pub unsafe fn write(&self, data: u32, mask: u32) {
			self.output.write(data);
			self.output_enable.write(mask);
		}
	}

	pub fn halt(success: bool) -> ! {
		unsafe {
			/* Stop the simulation */
			Gpio::gpio_a().write(if success {0} else {1}, 1);
			}
		/* Halt the hardware */
		loop {}
	}
}

fn benchmark_sha256_16(uart: &mut uart::Uart) {
	let mut input = [0; 16];
	let start = riscv::register::mcycle::read();
	for _ in 0..100 {
		let hash = sha2::Sha256::digest(&input);
		input.copy_from_slice(&hash[..16]);
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(  16 byte): {}", time / 100).unwrap();
}

fn benchmark_sha256_31(uart: &mut uart::Uart) {
	let mut input = [0; 31];
	let start = riscv::register::mcycle::read();
	for _ in 0..100 {
		let hash = sha2::Sha256::digest(&input);
		input.copy_from_slice(&hash[..31]);
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(  31 byte): {}", time / 100).unwrap();
}

fn benchmark_sha256_32(uart: &mut uart::Uart) {
	let mut input = [0; 32];
	let start = riscv::register::mcycle::read();
	for _ in 0..100 {
		let hash = sha2::Sha256::digest(&input);
		input[..32].copy_from_slice(hash.as_slice());
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(  32 byte): {}", time / 100).unwrap();
}

fn benchmark_sha256_63(uart: &mut uart::Uart) {
	let mut input = [0; 63];
	let start = riscv::register::mcycle::read();
	for _ in 0..50 {
		let hash = sha2::Sha256::digest(&input);
		input[..32].copy_from_slice(hash.as_slice());
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(  63 byte): {}", time / 50).unwrap();
}

fn benchmark_sha256_64(uart: &mut uart::Uart) {
	let mut input = [0; 64];
	let start = riscv::register::mcycle::read();
	for _ in 0..50 {
		let hash = sha2::Sha256::digest(&input);
		input[..32].copy_from_slice(hash.as_slice());
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(  64 byte): {}", time / 50).unwrap();
}

fn benchmark_sha256_256(uart: &mut uart::Uart) {
	let mut input = [0; 256];
	let start = riscv::register::mcycle::read();
	for _ in 0..20 {
		let hash = sha2::Sha256::digest(&input);
		input[..32].copy_from_slice(hash.as_slice());
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256( 256 byte): {}", time / 20).unwrap();
}

fn benchmark_sha256_1024(uart: &mut uart::Uart) {
	let mut input = [0; 1024];
	let start = riscv::register::mcycle::read();
	for _ in 0..10 {
		let hash = sha2::Sha256::digest(&input);
		input[..32].copy_from_slice(hash.as_slice());
	}
	let end = riscv::register::mcycle::read();
	let time = end - start;

	writeln!(uart, " sha256(1024 byte): {}", time / 10).unwrap();
}

// global_asm!(
// 	".global assembler_test",
// 	"assembler_test:",
// 	"nop",
// 	"insn i OPC_OP_IMM 0b001, t0, t1, 0b000000100000",
// 	".insn r CUSTOM_0, 0x6, 0x7f, x0, %0, %1",
// );
//global_asm!(include_str!("../../../test.S"));

mod mulacc {
	#[link(name = "mulacc-lib")]
	extern "C" {
		#[link_name = "mulacc_reset"]
		pub fn reset();

		#[link_name = "mulacc_muladd"]
		pub fn muladd(a: u32, b: u32, index: u32);

		#[link_name = "mulacc_read"]
		pub fn read(index: u32) -> u32;
	}
}

// fn assembler_test() {
// 	unsafe {
// 		asm!(".insn i OPC_OP_IMM 0b001, t0, t1, 0b000000100000");
// 	}
// }

#[riscv_rt::entry]
fn main() -> ! {
	let mut uart = uart::Uart::new();

	let mut string = arrayvec::ArrayString::<200>::new();
	writeln!(&mut string, "{} {:x} {:o}", 123, 123, 123).unwrap();
	uart.print(string.as_bytes());

	//2unsafe { assembler_test(); }
	unsafe {
		mulacc::reset();
		mulacc::muladd(1, 1, 0);
		writeln!(&mut uart, "{}", mulacc::read(0)).unwrap();
	}

	writeln!(&mut uart, "Hey, it didn't crash! :tada:").unwrap();

// 	write!(&mut uart, "Starting benchmark   '16' …").unwrap();
// 	benchmark_sha256_16(&mut uart);
// 	write!(&mut uart, "Starting benchmark   '31' …").unwrap();
// 	benchmark_sha256_31(&mut uart);
// 	write!(&mut uart, "Starting benchmark   '32' …").unwrap();
// 	benchmark_sha256_32(&mut uart);
// 	write!(&mut uart, "Starting benchmark   '63' …").unwrap();
// 	benchmark_sha256_63(&mut uart);
// 	write!(&mut uart, "Starting benchmark   '64' …").unwrap();
// 	benchmark_sha256_64(&mut uart);
// 	write!(&mut uart, "Starting benchmark  '256' …").unwrap();
// 	benchmark_sha256_256(&mut uart);
// 	write!(&mut uart, "Starting benchmark '1024' …").unwrap();
// 	benchmark_sha256_1024(&mut uart);

// 	writeln!(&mut uart, "Finished all benchmarks").unwrap();

	writeln!(&mut uart, "This is the end of the program. Goodbye.").unwrap();
	gpio::halt(true);
}

#[no_mangle]
#[allow(non_snake_case)]
fn ExceptionHandler(_trap_frame: &riscv_rt::TrapFrame) -> ! {
	gpio::halt(false);
}

#[inline(never)]
#[panic_handler]
#[no_mangle]
pub fn panic(info: &PanicInfo) -> ! {
	// Beware: panic = abort may be configured, making this dead code
	let mut uart = uart::Uart::new();
	let _ = writeln!(&mut uart, "{}", info);

	gpio::halt(false);
}
