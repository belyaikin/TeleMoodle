import { useState } from "react";
import Courses from "../components/Courses";
import Header from "../components/Header";


const courses = [
  {
    name: 'Calculus 1',
    teacher: 'Name Surname',
    attendance: '90%',
  },
  {
    name: 'Calculus 2',
    teacher: 'Another Name',
    attendance: '85%',
  },
  {
    name: 'Calculus 3',
    teacher: 'Name Surname',
    attendance: '95%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  },
  {
    name: 'Linear Algebra',
    teacher: 'Name Surname',
    attendance: '88%',
  }
];

export default function MainPage() {

    const [modal, setModal] = useState(false);


    return (
        <>
        <Header />
        <div className="course-container">
            <div className="course-display">
            {courses.map((course, index) => (
                <Courses
                setModal={setModal}
                key={index}
                name={course.name}
                teacher={course.teacher}
                attendance={course.attendance}
                />
            ))}
            </div>
            {modal && (
                <div className="modal-backdrop" onClick={() => setModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close-btn" onClick={() => setModal(false)}>X</button>
                        <h1 className='modal-h1'>Information</h1>
                    </div>
                </div>   
            ) }
        </div>


        </>
    );
}
